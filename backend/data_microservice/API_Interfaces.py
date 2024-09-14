import uuid
from datetime import datetime, timedelta

import requests
import pycountry
import logging
import os
import json
import pandas as pd
from pydantic import BaseModel
from sqlalchemy.exc import IntegrityError

from gen_utils.general_API_classes import (
    NewsInfo,
    SpaceEvents,
    LaunchInfo,
    EventType,
    DonkiNotification,
    NEO,
    SolarEventsInfo,
)
from gen_utils.load_envs import NASA_API, NEWS_API, DATABASE_URL
from gen_utils.general_API_classes import NewsTopics


from sqlmodel import Field, SQLModel, create_engine, Session
from enum import Enum, IntEnum

ECLIPSE_MAP = {"P": "Partial", "A": "Annular", "T": "Total", "H": "Hybrid"}


CACHE = True


def handleCoordinates(longlat):
    return float(longlat[:-1]) if longlat[-1] in ["E", "N"] else -float(longlat[:-1])


engine = create_engine(DATABASE_URL, echo=False)


class BaseAPIInterface:
    def __init__(self, logger):
        self.cache = CACHE
        self.logger = logger

    def baseModelToKV(self, data):
        keys, values = [], []
        for k, v in data.__dict__.items():
            keys.append(k)
            values.append(v)
        return keys, values

    def APIName(self):
        raise NotImplementedError

    def addDataToDB(self, data):
        with Session(engine) as session:
            for entry in data:
                try:
                    session.add(SpaceEvents(**entry.model_dump()))
                    session.flush()
                    session.add(entry)
                    session.commit()
                except IntegrityError as e:
                    # raise e
                    self.logger.info("entry in DB")
                    session.rollback()

    @staticmethod
    def createDBTables():
        SQLModel.metadata.create_all(engine, checkfirst=True)

    def readAPI(self):
        raise NotImplementedError

    def loadData(self):
        if self.cache:
            result = self.fromJson()
        else:
            result = self.readAPI()
            self.toJson(result)

        data = self.parseData(result)
        self.logger.info("Adding data to database")
        self.addDataToDB(data)
        self.logger.info("Added data to database")

    def parseData(self, data) -> BaseModel:
        raise NotImplementedError

    def toJson(self, result: dict):
        with open(
            os.path.join(
                os.path.dirname(__file__), "cached_data", self.APIName() + ".json"
            ),
            "w",
        ) as jsonfile:
            json.dump(result, jsonfile)

    def fromJson(self) -> dict:
        with open(
            os.path.join(
                os.path.dirname(__file__), "cached_data", self.APIName() + ".json"
            ),
            "r",
        ) as jsonfile:
            result = json.load(jsonfile)
        return result


class LaunchAPI(BaseAPIInterface):
    def __init__(self, logger):
        super(LaunchAPI, self).__init__(logger)

    def APIName(self):
        return "RocketLaunchData"

    def readAPI(self):
        return requests.get("https://ll.thespacedevs.com/2.2.0/launch/").json()

    def parseData(self, data) -> list[BaseModel]:
        parsedData = []

        for launch in data["results"]:
            dataModel = LaunchInfo(
                event_id=uuid.uuid4(),
                event_type=EventType.launch,
                name=launch["name"],
                status_code=launch["status"]["abbrev"],
                status_desc=launch["status"]["description"],
                last_updated=datetime.strptime(
                    launch["last_updated"], "%Y-%m-%dT%H:%M:%SZ"
                ),
                service_provider=launch["launch_service_provider"]["type"],
                rocket_name=launch["rocket"]["configuration"]["name"],
                mission_name=launch["mission"]["name"],
                mission_type=launch["mission"]["type"],
                mission_info=launch["mission"]["description"],
                longitude=float(launch["pad"]["longitude"]),
                latitude=float(launch["pad"]["latitude"]),
                pad_name=launch["pad"]["location"]["name"],
                country=pycountry.countries.get(
                    alpha_3=launch["pad"]["location"]["country_code"]
                ).name,
                launch_image=launch["image"],
            )
            parsedData.append(dataModel)

        return parsedData


class DONKIApi(BaseAPIInterface):
    def __init__(self, logger):
        super(DONKIApi, self).__init__(logger)

    def APIName(self):
        return "DonkiData"

    def readAPI(self):
        return requests.get(
            "https://api.nasa.gov/DONKI/notifications",
            params={
                "end_date": "2024-07-04",
                "start_date": "2024-07-10",
                "api_key": NASA_API,
                "type": "all",
            },
        ).json()

    def parseData(self, data) -> list[BaseModel]:
        parsedData = []
        for notif in data:
            dataModel = DonkiNotification(
                event_id=uuid.uuid4(),
                event_type=EventType.donki,
                message_id=notif["messageID"],
                message_type=notif["messageType"],
                message_url=notif["messageURL"],
                message_issue_time=datetime.strptime(
                    notif["messageIssueTime"], "%Y-%m-%dT%H:%MZ"
                ),
                message_body=notif["messageBody"],
            )
            parsedData.append(dataModel)
        return parsedData


class NEOApi(BaseAPIInterface):
    def __init__(self, logger):
        super(NEOApi, self).__init__(logger)

    def APIName(self):
        return "NEOData"

    def readAPI(self):
        return requests.get(
            "https://api.nasa.gov/neo/rest/v1/feed",
            params={
                "end_date": "2024-07-04",
                "start_date": "2024-07-10",
                "api_key": NASA_API,
                "type": "all",
            },
        ).json()

    def parseData(self, data) -> list[BaseModel]:
        parsedData = []
        for day, events in data["near_earth_objects"].items():
            for item in events:
                dataModel = NEO(
                    event_id=uuid.uuid4(),
                    event_type=EventType.neo,
                    name=item["name"],
                    diameter=float(
                        item["estimated_diameter"]["kilometers"][
                            "estimated_diameter_max"
                        ]
                    ),
                    is_hazardous=item["is_potentially_hazardous_asteroid"],
                    approach_date=datetime.strptime(
                        item["close_approach_data"][0]["close_approach_date_full"],
                        "%Y-%b-%d %H:%M",
                    ),
                    speed=float(
                        item["close_approach_data"][0]["relative_velocity"][
                            "kilometers_per_second"
                        ]
                    ),
                    miss_distance=item["close_approach_data"][0]["miss_distance"][
                        "kilometers"
                    ],
                )
                parsedData.append(dataModel)
        return parsedData


class EclipseAPI(BaseAPIInterface):
    def __init__(self, logger):
        super(EclipseAPI, self).__init__(logger)

    def APIName(self):
        return "EclipseData"

    def readAPI(self):
        df = pd.read_csv("cached_data/Eclipses.csv")
        return df.to_json()

    def parseData(self, data) -> list[BaseModel]:
        parsedData = []
        data = pd.read_json(data)
        for row in data.itertuples():
            if pd.isna(row.CentralDur):
                t = timedelta()
            else:
                t = datetime.strptime(row.CentralDur, "%Mm%Ss")
                t = timedelta(minutes=t.minute, seconds=t.second)

            try:
                pathWidth = int(row.PathWidthkm) if row.PathWidthkm != "-" else 0
            except:
                pathWidth = 0

            dataModel = SolarEventsInfo(
                event_id=uuid.uuid4(),
                event_type=EventType.solar,
                longitude=handleCoordinates(row.Long),
                latitude=handleCoordinates(row.Lat),
                timestamp=datetime.strptime(row.CalendarDate, "%Y-%b-%d"),
                central_duration=t,
                path_width=pathWidth,
                eclipse_type=ECLIPSE_MAP.get(row.EclType[0], row.EclType),
            )
            parsedData.append(dataModel)
        return parsedData


class NewsAPI(BaseAPIInterface):
    def __init__(self, logger):
        super(NewsAPI, self).__init__(logger)

    def APIName(self):
        return "NewsAPI"

    def readAPI(self):
        articles = {}
        for newsTopic in NewsTopics:
            articles[newsTopic.value] = requests.get(
                "https://newsapi.org/v2/everything",
                params={
                    "q": f'space "{newsTopic.value}"',
                    "sortBy": "publishedAt",
                    "from": "2024-06-30",
                    "apiKey": NEWS_API,
                    "language": "en",
                    "searchIn": "title,description",
                    "domains": "apod.nasa.gov,cnn.com,space.com,nasa.gov,science.nasa.gov,cbc.ca,phys.org,sciencedaily.com",
                },
            ).json()
        return articles

    def parseData(self, data) -> list[BaseModel]:
        parsedData = []
        for topic, articles in data.items():
            for article in articles["articles"]:
                print(topic)
                dataModel = NewsInfo(
                    article_id=uuid.uuid4(),
                    topic=NewsTopics(topic),
                    author=article["author"],
                    title=article["title"],
                    description=article["description"],
                    url=article["url"],
                    url_to_image=article["urlToImage"],
                    published_at=article["publishedAt"],
                    content=article["content"],
                )
                if dataModel.author is None or dataModel.url_to_image is None:
                    continue
                parsedData.append(dataModel)
        return parsedData

    def addDataToDB(self, data):
        with Session(engine) as session:
            for entry in data:
                try:
                    session.add(entry)
                    session.commit()
                except IntegrityError as e:
                    self.logger.info("entry in DB")
                    session.rollback()


APIS = [NEOApi, DONKIApi, LaunchAPI, EclipseAPI, NewsAPI]


BaseAPIInterface.createDBTables()


def main(logger):
    SQLModel.metadata.drop_all(bind=engine)
    BaseAPIInterface.createDBTables()
    #
    LaunchAPI(logger).loadData()
    DONKIApi(logger).loadData()
    NEOApi(logger).loadData()
    EclipseAPI(logger).loadData()
    NewsAPI(logger).loadData()


if __name__ == "__main__":
    logger = logging.getLogger(__name__)
    logger.setLevel(logging.DEBUG)

    main(logger)
    #
    # with Session(engine) as session:
    #     result = session.execute(select(LaunchInfo))

    # res = UserEventComments(
    #     comment_id = uuid.uuid4(),
    #     event_id = result.all()[-1]._tuple()[0].event_id,
    #     name = "test",
    #     timestamp = datetime.now(),
    #     comment = "tes2t3"
    # )
    # eid = result.all()[-1]._tuple()[0].event_id
    # print(eid)
    # result = session.execute(select(UserEventComments).where(UserEventComments.event_id == eid))
    # print(result.all())
    # session.add(res)
    # session.commit()
