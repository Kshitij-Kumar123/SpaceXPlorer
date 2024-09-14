import logging

from typing import Union, Optional, List
from sqlalchemy import or_
from gen_utils.general_API_classes import (
    UserSession,
    NewsInfo,
    UserProfiles,
    NewsTopics,
    UserLikes,
    get_headers,
)
from contextlib import asynccontextmanager


from API_Interfaces import NEOApi, DONKIApi, LaunchAPI, EclipseAPI, NewsAPI, EventType
from fastapi import Query, Depends
from sqlmodel import select
from gen_utils.load_envs import DATABASE_URL
from fastapi import FastAPI, status, HTTPException
from gen_utils.utils import validateSession
from gen_utils.load_envs import CACHE
from sqlmodel import Field, SQLModel, create_engine, Session
from gen_utils.general_API_classes import (
    API_MAPPING,
    NEO,
    DonkiNotification,
    LaunchInfo,
    SolarEventsInfo,
    UserNewsComments,
    UserEventComments,
)
import uuid

logger = logging.getLogger("uvicorn.error")
logger.setLevel(logging.INFO)

APIS = [NEOApi, DONKIApi, LaunchAPI, EclipseAPI, NewsAPI]


engine = create_engine(DATABASE_URL, echo=False)


@asynccontextmanager
async def lifespan(app: FastAPI):
    if CACHE:
        [api(logger).loadData() for api in APIS]
    logging.info("Initialized database")
    yield


app = FastAPI(title="Data Microservice", lifespan=lifespan)


@app.get("/events/all")
async def get_events(
    userSession: UserSession = Depends(get_headers),
    filter: List[EventType] = Query(
        ...,
        description="A list of particular types of events you want returned, if empty user specific events are returned",
    ),
) -> list[Union[NEO | DonkiNotification | LaunchInfo | SolarEventsInfo]]:
    validateSession(userSession.username, userSession.session_id)
    events = []

    with Session(engine) as session:
        userProfile = session.execute(
            select(UserProfiles).where(UserProfiles.username == userSession.username)
        ).fetchone()[0]
        for dataTypes in [API_MAPPING[apiType] for apiType in filter]:
            user_likes_subquery = (
                select(UserLikes.item_id, UserLikes.like_status)
                .filter(UserLikes.username == userSession.username)
                .subquery()
            )

            result = session.execute(
                select(dataTypes, user_likes_subquery.c.like_status)
                .outerjoin(
                    user_likes_subquery,
                    user_likes_subquery.c.item_id == dataTypes.event_id,
                )
                .order_by(dataTypes.likes.desc(), dataTypes.comment_count.desc())
            )
            for item in result.all():
                item[0].is_subscribed = item[0].event_id in set(
                    userProfile.subscriptions
                )
                item[0].like_status = item[1]
                events.append(item[0])
    return events


@app.get("/events/user")
async def get_events(
    userSession: UserSession = Depends(get_headers),
) -> list[Union[NEO | DonkiNotification | LaunchInfo | SolarEventsInfo]]:
    validateSession(userSession.username, userSession.session_id)
    events = []

    logger.info("Returning user specific events")
    with Session(engine) as session:
        userProfile = session.execute(
            select(UserProfiles).where(UserProfiles.username == userSession.username)
        ).fetchone()[0]
        if userProfile.subscriptions:
            for eventType in EventType:
                query = select(API_MAPPING[eventType], UserLikes.like_status).join(
                    UserLikes,
                    UserLikes.item_id == API_MAPPING[eventType].event_id,
                    isouter=True,
                )
                query = (
                    query.where(
                        or_(
                            *[
                                API_MAPPING[eventType].event_id == event_id
                                for event_id in userProfile.subscriptions
                            ]
                        )
                    )
                    .filter(
                        or_(
                            (UserLikes.username == userSession.username),
                            (UserLikes.username.is_(None)),
                        )
                    )
                    .order_by(
                        API_MAPPING[eventType].likes.desc(),
                        API_MAPPING[eventType].comment_count.desc(),
                    )
                )

                result = session.execute(query).fetchall()
                for event, like_status in result:
                    event.is_subscribed = event.event_id in set(
                        userProfile.subscriptions
                    )
                    if like_status:
                        event.like_status = like_status
                    events.append(event)
    return events


@app.get("/news")
async def get_news(userSession: UserSession = Depends(get_headers)) -> list[NewsInfo]:
    validateSession(userSession.username, userSession.session_id)
    newsArticles = []

    with Session(engine) as session:
        result = session.execute(
            select(UserProfiles).where(UserProfiles.username == userSession.username)
        ).fetchone()[0]
        if result is None:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="User does not subscribed to any topics",
            )

        user_likes_subquery = (
            select(UserLikes.item_id, UserLikes.like_status)
            .filter(UserLikes.username == userSession.username)
            .subquery()
        )

        query = (
            select(NewsInfo, user_likes_subquery.c.like_status)
            .where(
                or_(
                    *[
                        NewsInfo.topic == NewsTopics(userTopic).name
                        for userTopic in result.news_topics
                    ]
                )
            )
            .outerjoin(
                user_likes_subquery,
                user_likes_subquery.c.item_id == NewsInfo.article_id,
            )
            .order_by(NewsInfo.likes.desc(), NewsInfo.comment_count.desc())
        )

        result = session.execute(query).fetchall()

        for article, like_status in result:
            if like_status:
                article.like_status = like_status
            newsArticles.append(article)

    return newsArticles


@app.get("/news/comments")
async def get_news_comments(
    userSession: UserSession = Depends(get_headers),
    article_id: uuid.UUID = Query(description="The articles UUID"),
) -> list[UserNewsComments]:
    validateSession(userSession.username, userSession.session_id)
    newsComments = []
    with Session(engine) as session:
        user_likes_subquery = (
            select(UserLikes.item_id, UserLikes.like_status)
            .filter(UserLikes.username == userSession.username)
            .subquery()
        )

        query = (
            (
                select(UserNewsComments, user_likes_subquery.c.like_status).outerjoin(
                    user_likes_subquery,
                    user_likes_subquery.c.item_id == UserNewsComments.comment_id,
                )
            )
            .where(UserNewsComments.article_id == article_id)
            .order_by(UserNewsComments.timestamp.asc())
        )

        result = session.execute(query).fetchall()
        for newsComment, like_status in result:
            if like_status:
                newsComment.like_status = like_status
            newsComments.append(newsComment)
    return newsComments


@app.get("/events/comments")
async def get_events_comments(
    userSession: UserSession = Depends(get_headers),
    event_id: uuid.UUID = Query(description="The events UUID"),
) -> list[UserEventComments]:
    validateSession(userSession.username, userSession.session_id)
    eventComments = []
    with Session(engine) as session:
        user_likes_subquery = (
            select(UserLikes.item_id, UserLikes.like_status)
            .filter(UserLikes.username == userSession.username)
            .subquery()
        )

        query = (
            (
                select(UserEventComments, user_likes_subquery.c.like_status).outerjoin(
                    user_likes_subquery,
                    user_likes_subquery.c.item_id == UserEventComments.comment_id,
                )
            )
            .where(UserEventComments.event_id == event_id)
            .order_by(UserEventComments.timestamp.asc())
        )

        result = session.execute(query).fetchall()

        for eventComment, like_status in result:
            if like_status:
                eventComment.like_status = like_status
            eventComments.append(eventComment)
    return eventComments


if __name__ == "__main__":
    import uvicorn
    import yaml

    openapi = app.openapi()
    version = openapi.get("openapi", "version")
    with open("spec.yaml", "w") as f:
        yaml.dump(openapi, f, sort_keys=False)

    uvicorn.run(app, host="0.0.0.0", port=4342)
