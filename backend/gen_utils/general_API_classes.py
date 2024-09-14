import uuid

from typing import Optional, List, Annotated

from fastapi import Header
from sqlalchemy import UniqueConstraint, UUID, PrimaryKeyConstraint
from pydantic import BaseModel
from datetime import datetime
from sqlmodel import Field, SQLModel
from enum import Enum
from sqlalchemy import Column
from sqlalchemy.dialects.postgresql import ARRAY
from sqlalchemy.types import String

import uuid

from pydantic import BaseModel
from datetime import datetime, timedelta

from sqlmodel import Field, SQLModel, create_engine, Session
from enum import Enum, IntEnum


class Annotations:
    username = Field(..., description="The users username")
    password = Field(..., description="The users password")
    email = Field(..., description="The users email address")
    phone_number = Field(..., description="The users phone number")
    news_topics = Field(
        ...,
        description="The news topics that a user is subscribed to, must be part of the selected enumerations",
    )
    subscriptions = Field(
        ...,
        description="The space event subscriptions that a user is subscribed to, must be part of the selected enumerations",
    )
    event_id = Field(..., description="The UUID associated with a particular event")
    article_id = Field(..., description="The UUID associated with a particular article")
    comment_id = Field(..., description="The UUID associated with a particular comment")
    session_id = Field(
        ...,
        description="The sessionID (UUID) that is returned after a user successfully logs in",
    )

    likes = Field(
        0, description="The number of likes a particular Article/Event/Comment has"
    )
    dislikes = Field(
        0, description="The number of dislikes a particular Article/Event/Comment has"
    )

    comment_count = Field(
        0, description="The number of comments an article or event has"
    )
    event_type = Field(
        ..., description="The particular space event class associated with the event"
    )
    message = Field(..., description="An informative message regarding the return")
    timestamp = Field(..., description="The server side time that the item was created")

    comment = Field(..., description="The text contained in the comment")
    is_subscribed = Field(
        False, description="Whether the user is subscribed to the current event"
    )


class UserCredentials(BaseModel):
    username: str = Annotations.username
    password: str = Annotations.password


class UserSession(BaseModel):
    username: str = Annotations.username
    session_id: uuid.UUID = Annotations.username

    # username: str # = Field(None, alias="X-Custom-Header") # Header(None, description=Annotations.username.description)
    # session_id: uuid.UUID # = Header(None, description=Annotations.username.description)


def get_headers(
    username: Optional[str] = Header(..., alias="Auth-Username"),
    session_id: Optional[str] = Header(..., alias="Auth-SessionID"),
) -> UserSession:
    if username is None or session_id is None:
        print("bob!")
    return UserSession(username=username, session_id=session_id)


class SubscriptionTopics(Enum):
    topic1 = "solar"
    topic2 = "donki"
    topic3 = "launch"
    topic4 = "neo"


class NewsTopics(Enum):
    Planets = "Planets"
    Eclipses = "Eclipses"
    Astronauts = "Astronauts"
    NASA = "NASA"
    # Comets = "Comets"
    Asteroids = "Asteroids"
    SolarSystem = "Solar System"


class LikeStatus(Enum):
    like = "like"
    dislike = "dislike"
    neutral = "neutral"


class UserProfiles(SQLModel, table=True):
    username: str = Field(
        default=None, primary_key=True, description=Annotations.username.description
    )
    password: str = Annotations.password
    email: str = Annotations.email
    phone_number: str = Annotations.phone_number
    news_topics: List[NewsTopics] = Field(
        default=[],
        sa_column=Column(ARRAY(String)),
        description=Annotations.news_topics.description,
    )
    subscriptions: List[uuid.UUID] = Field(
        default=[],
        sa_column=Column(ARRAY(UUID)),
        description=Annotations.subscriptions.description,
    )

    # liked_news_articles: List[uuid.UUID] = Field(
    #     default=[],
    #     sa_column=Column(ARRAY(UUID)),
    #
    # )
    # liked_events: List[uuid.UUID] = Field(
    #     default=[],
    #     sa_column=Column(ARRAY(UUID)),
    #
    # )
    # liked_news_comments: List[uuid.UUID] = Field(
    #     default=[],
    #     sa_column=Column(ARRAY(UUID)),
    #
    # )
    # liked_event_comments: List[uuid.UUID] = Field(
    #     default=[],
    #     sa_column=Column(ARRAY(UUID)),
    #
    # )


class EventType(str, Enum):
    neo = "neo"
    donki = "donki"
    launch = "launch"
    solar = "solar"


class LikeType(Enum):
    eventLike = "event_like"
    newsLike = " news_like"
    eventCommentLike = "event_comment_like"
    newsCommentLike = "news_comment_like"


class SpaceEventBase(SQLModel):
    event_id: uuid.UUID = Field(
        default_factory=uuid.uuid4,
        primary_key=True,
        foreign_key="spaceevents.event_id",
        description=Annotations.event_id.description,
    )
    likes: int = Annotations.likes
    dislikes: int = Annotations.dislikes
    comment_count: int = Annotations.comment_count
    like_status: LikeStatus = Field(default=LikeStatus.neutral, nullable=True)
    event_type: EventType = Annotations.event_type
    is_subscribed: bool = Annotations.is_subscribed


class SpaceEvents(SpaceEventBase, table=True):
    pass


class GenericReturn(BaseModel):
    message: str = Annotations.message
    detail: Optional[str]


class ItemLikes(BaseModel):
    likes: int = Annotations.likes
    dislikes: int = Annotations.dislikes


class UserEventComments(SQLModel, table=True):
    comment_id: uuid.UUID = Field(
        default_factory=uuid.uuid4,
        primary_key=True,
        description=Annotations.comment_id.description,
    )
    event_id: uuid.UUID = Field(
        default=None,
        foreign_key="spaceevents.event_id",
        description=Annotations.event_id.description,
    )
    likes: int = Annotations.likes
    dislikes: int = Annotations.dislikes
    like_status: LikeStatus = Field(default=LikeStatus.neutral, nullable=True)
    username: str = Annotations.username
    timestamp: datetime = Annotations.timestamp
    comment: str = Annotations.comment


class NewsInfo(SQLModel, table=True):
    article_id: uuid.UUID = Field(
        default=None, primary_key=True, description=Annotations.article_id.description
    )
    likes: int = Annotations.likes
    dislikes: int = Annotations.dislikes
    comment_count: int = Annotations.comment_count
    like_status: LikeStatus = Field(default=LikeStatus.neutral, nullable=True)
    topic: NewsTopics
    author: str
    title: str
    description: str
    url: str
    url_to_image: str
    published_at: str
    content: str

    __table_args__ = (
        UniqueConstraint("author", "title", name="_unique_constraint_news"),
    )


#
class UserLikes(SQLModel, table=True):
    username: str = Field(
        default=None, primary_key=True, description=Annotations.username.description
    )
    item_id: uuid.UUID = Field(
        default=None,
        primary_key=True,
        nullable=True,
    )

    like_status: LikeStatus = Field(default=LikeStatus.neutral, nullable=True)

    __table_args__ = (PrimaryKeyConstraint("username", "item_id"),)


class UserNewsComments(SQLModel, table=True):
    comment_id: uuid.UUID = Field(
        default_factory=uuid.uuid4,
        primary_key=True,
        description=Annotations.comment_id.description,
    )
    article_id: uuid.UUID = Field(
        default=None,
        foreign_key="newsinfo.article_id",
        description=Annotations.article_id.description,
    )
    likes: int = Annotations.likes
    dislikes: int = Annotations.dislikes
    like_status: LikeStatus = Field(default=LikeStatus.neutral, nullable=True)
    username: str = Annotations.username
    timestamp: datetime = Annotations.timestamp
    comment: str = Annotations.comment


from sqlalchemy import UniqueConstraint


class NEO(SpaceEventBase, table=True):
    name: str
    diameter: float
    is_hazardous: bool
    approach_date: datetime
    speed: float
    miss_distance: str
    __table_args__ = (
        UniqueConstraint(
            "name",
            "diameter",
            "is_hazardous",
            "approach_date",
            "speed",
            "miss_distance",
            name="_unique_constraint_neo",
        ),
    )


class DonkiNotification(SpaceEventBase, table=True):
    message_id: str
    message_type: str
    message_url: str
    message_issue_time: datetime
    message_body: str

    __table_args__ = (
        UniqueConstraint(
            "message_id",
            "message_type",
            "message_url",
            "message_issue_time",
            name="_unique_constraint_donki",
        ),
    )


class SolarEventsInfo(SpaceEventBase, table=True):
    longitude: float
    latitude: float
    timestamp: datetime
    central_duration: timedelta
    path_width: int
    eclipse_type: str
    __table_args__ = (
        UniqueConstraint(
            "longitude",
            "latitude",
            "timestamp",
            "central_duration",
            "path_width",
            "eclipse_type",
            name="_unique_constraint_solar",
        ),
    )


class LaunchInfo(SpaceEventBase, table=True):
    longitude: float
    latitude: float
    country: str
    pad_name: str
    name: str
    status_code: str
    status_desc: str
    last_updated: datetime
    service_provider: str
    rocket_name: str
    mission_name: str
    mission_type: str
    mission_info: str
    launch_image: str

    __table_args__ = (
        UniqueConstraint(
            "longitude",
            "latitude",
            "country",
            "pad_name",
            "name",
            "status_code",
            "status_desc",
            "last_updated",
            "service_provider",
            "rocket_name",
            "mission_name",
            "mission_type",
            "mission_info",
            "launch_image",
            name="_unique_constraint_launch",
        ),
    )


API_MAPPING = {
    EventType.neo: NEO,
    EventType.solar: SolarEventsInfo,
    EventType.donki: DonkiNotification,
    EventType.launch: LaunchInfo,
}
