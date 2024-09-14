import datetime
import uuid
import logging

from sqlalchemy import create_engine
from sqlmodel import Session
from sqlmodel import select
from gen_utils.load_envs import DATABASE_URL
from gen_utils.utils import *
from contextlib import asynccontextmanager
from gen_utils.general_API_classes import (
    UserEventComments,
    UserNewsComments,
    ItemLikes,
    UserSession,
    SpaceEvents,
    NewsInfo,
    UserProfiles,
    SubscriptionTopics,
    NewsTopics,
    Annotations,
    UserLikes,
    LikeStatus,
    LikeType,
    EventType,
    API_MAPPING,
    get_headers,
)
from enum import Enum
from fastapi import FastAPI, Query, Header, Depends

logger = logging.getLogger("uvicorn.error")
logger.setLevel(logging.INFO)

engine = create_engine(DATABASE_URL, echo=False)


class LikeDislikeAction(Enum):
    like = "like"
    dislike = "dislike"
    unlike = "unlike"
    undislike = "undislike"


@asynccontextmanager
async def lifespan(app: FastAPI):
    logging.info("Initialized database")
    yield


app = FastAPI(title="User Interactions Microservice", lifespan=lifespan)


# region EVENTS
def processLikes(session, record, action, username, itemID):
    userLikes = session.execute(
        select(UserLikes)
        .where(UserLikes.item_id == itemID)
        .where(UserLikes.username == username)
    ).first()
    if userLikes:
        print("Existing query")
        userLikes = userLikes[0]
    else:
        userLikes = UserLikes(
            username=username,
            item_id=itemID,
            like_status=LikeStatus.neutral,
        )

    if action == action.like:
        if userLikes.like_status == LikeStatus.dislike:
            record.dislikes -= 1
        record.likes += 1
        userLikes.like_status = LikeStatus.like
    elif action == action.dislike:
        if userLikes.like_status == LikeStatus.like:
            record.likes -= 1
        record.dislikes += 1
        userLikes.like_status = LikeStatus.dislike
    elif action == action.unlike:
        record.likes -= 1
        userLikes.like_status = LikeStatus.neutral
    elif action == action.undislike:
        record.dislikes -= 1
        userLikes.like_status = LikeStatus.neutral
    session.add(userLikes)
    session.add(record)
    session.commit()
    session.refresh(record)
    return record


@app.put(
    "/events/likes/{action}",
    response_model=ItemLikes,
    responses={200: {"description": "The number of likes/dislikes the event now has"}},
    description="Adds a like to a particular event",
)
async def put_events_likes(
    action: LikeDislikeAction,
    userSession: UserSession = Depends(get_headers),
    event_id: uuid.UUID = Query(..., description=Annotations.event_id.description),
) -> ItemLikes:
    validateSession(userSession.username, userSession.session_id)

    with Session(engine) as session:
        record = session.execute(
            select(SpaceEvents).where(SpaceEvents.event_id == event_id)
        ).one()[0]

        eventTyped = session.execute(
            select(API_MAPPING[record.event_type]).where(
                API_MAPPING[record.event_type].event_id == event_id
            )
        ).one()[0]

        record = processLikes(
            session, eventTyped, action, userSession.username, event_id
        )

    return ItemLikes(likes=record.likes, dislikes=record.dislikes)


@app.put(
    "/events/comments",
    responses={200: {"description": "The comment the user created"}},
    description="Adds a comment to a particular event",
)
async def put_events_comments(
    userSession: UserSession = Depends(get_headers),
    event_id: uuid.UUID = Query(..., description="The events UUID"),
    comment: str = Query(..., description="The users comment"),
) -> UserEventComments:
    validateSession(userSession.username, userSession.session_id)

    userComment = UserEventComments(
        event_id=event_id,
        username=userSession.username,
        timestamp=datetime.datetime.now(),
        comment=comment,
    )

    with Session(engine) as session:
        record = session.execute(
            select(SpaceEvents).where(SpaceEvents.event_id == event_id)
        ).one()[0]

        eventRecord = session.execute(
            select(API_MAPPING[record.event_type]).where(
                API_MAPPING[record.event_type].event_id == event_id
            )
        ).one()[0]
        eventRecord.comment_count += 1
        session.add(eventRecord)
        session.commit()
        session.refresh(eventRecord)

        session.add(userComment)
        session.commit()
        session.refresh(userComment)

    return userComment


@app.put(
    "/events/comments/likes/{action}",
    response_model=ItemLikes,
    responses={
        200: {"description": "The number of likes/dislikes the events comment now has"}
    },
    description="Adds or removes likes/dislikes to a particular events comment",
)
async def put_events_comments_likes(
    action: LikeDislikeAction,
    userSession: UserSession = Depends(get_headers),
    event_id: uuid.UUID = Query(..., description="The events UUID"),
    comment_id: uuid.UUID = Query(..., description="The comments UUID"),
) -> ItemLikes:
    validateSession(userSession.username, userSession.session_id)

    with Session(engine) as session:
        record = session.execute(
            select(UserEventComments)
            .where(UserEventComments.event_id == event_id)
            .where(UserEventComments.comment_id == comment_id)
        ).one()[0]

        record = processLikes(session, record, action, userSession.username, comment_id)

    return ItemLikes(likes=record.likes, dislikes=record.dislikes)


# endregion EVENTS


# region NEWS


@app.put(
    "/news/likes/{action}",
    response_model=ItemLikes,
    responses={
        200: {"description": "The number of likes/dislikes the article now has"}
    },
    description="Adds or removes likes/dislikes to a particular article ",
)
async def put_news_likes(
    action: LikeDislikeAction,
    userSession: UserSession = Depends(get_headers),
    article_id: uuid.UUID = Query(..., description=Annotations.article_id.description),
) -> ItemLikes:
    validateSession(userSession.username, userSession.session_id)

    with Session(engine) as session:
        record = session.execute(
            select(NewsInfo).where(NewsInfo.article_id == article_id)
        ).one()[0]

        record = processLikes(session, record, action, userSession.username, article_id)

    return ItemLikes(likes=record.likes, dislikes=record.dislikes)


@app.put(
    "/news/comments",
    responses={200: {"description": "The comment the user created"}},
    description="Adds a comment to a particular news article",
)
async def put_news_comments(
    userSession: UserSession = Depends(get_headers),
    article_id: uuid.UUID = Query(..., description=Annotations.article_id.description),
    comment: str = Query(..., description=Annotations.comment.description),
) -> UserNewsComments:
    userComment = UserNewsComments(
        article_id=article_id,
        username=userSession.username,
        timestamp=datetime.datetime.now(),
        comment=comment,
    )

    with Session(engine) as session:
        record = session.execute(
            select(NewsInfo).where(NewsInfo.article_id == article_id)
        ).one()[0]
        record.comment_count += 1
        session.add(record)
        session.commit()
        session.refresh(record)

        session.add(userComment)
        session.commit()
        session.refresh(userComment)
    return userComment


@app.put(
    "/news/comments/likes/{action}",
    response_model=ItemLikes,
    responses={
        200: {"description": "The number of likes/dislikes the news comment now has"}
    },
    description="Adds or removes likes/dislikes to a particular news articles comment",
)
async def put_news_comments_likes(
    action: LikeDislikeAction,
    userSession: UserSession = Depends(get_headers),
    article_id: uuid.UUID = Query(..., description=Annotations.article_id.description),
    comment_id: uuid.UUID = Query(..., description=Annotations.comment_id.description),
) -> ItemLikes:
    validateSession(userSession.username, userSession.session_id)

    with Session(engine) as session:
        record = session.execute(
            select(UserNewsComments)
            .where(UserNewsComments.article_id == article_id)
            .where(UserNewsComments.comment_id == comment_id)
        ).one()[0]

        record = processLikes(session, record, action, userSession.username, comment_id)

    return ItemLikes(likes=record.likes, dislikes=record.dislikes)


# endregion NEWS


# region USERS


@app.put(
    "/users/subscriptions",
    responses={200: {"description": "The users subscriptions"}},
    description="Adds an event to a users subscription list",
)
async def put_users_subscriptions(
    userSession: UserSession = Depends(get_headers),
    event_id: uuid.UUID = Query(..., description=Annotations.event_id.description),
) -> list[uuid.UUID]:
    validateSession(userSession.username, userSession.session_id)

    with Session(engine) as session:
        record = session.execute(
            select(UserProfiles).where(UserProfiles.username == userSession.username)
        ).one()[0]

        # weird mem address stuff
        newSubs = record.subscriptions.copy()
        newSubs.append(event_id)

        record.subscriptions = set(newSubs)
        session.add(record)
        session.commit()
        session.refresh(record)
        print(record)
    return record.subscriptions


@app.delete(
    "/users/subscriptions",
    responses={200: {"description": "The users subscriptions"}},
    description="Removes an event a user is subscribed to",
)
async def delete_users_subscriptions(
    userSession: UserSession = Depends(get_headers),
    event_id: uuid.UUID = Query(..., description=Annotations.event_id.description),
) -> list[uuid.UUID]:
    validateSession(userSession.username, userSession.session_id)

    with Session(engine) as session:
        record = session.execute(
            select(UserProfiles).where(UserProfiles.username == userSession.username)
        ).one()[0]
        newSubs = record.subscriptions.copy()
        newSubs.remove(event_id)

        record.subscriptions = newSubs
        session.add(record)
        session.commit()
        session.refresh(record)
        print(record)
    return record.subscriptions


@app.get(
    "/users/subscriptions",
    responses={200: {"description": "The users subscriptions"}},
    description="Gets the users current space event subscriptions",
)
async def get_users_subscriptions(
    userSession: UserSession = Depends(get_headers),
) -> list[uuid.UUID]:
    validateSession(userSession.username, userSession.session_id)

    with Session(engine) as session:
        record = session.execute(
            select(UserProfiles).where(UserProfiles.username == userSession.username)
        ).one()[0]
    print(record)
    return record.subscriptions


@app.put(
    "/users/topics",
    responses={200: {"description": "The users news topics"}},
    description="Updates users subscribed news topics",
)
async def put_users_topics(
    userSession: UserSession = Depends(get_headers),
    topics: list[NewsTopics] = Query(
        ..., description="A list of news topics to subscribe to"
    ),
) -> list[NewsTopics]:
    validateSession(userSession.username, userSession.session_id)
    with Session(engine) as session:
        record = session.execute(
            select(UserProfiles).where(UserProfiles.username == userSession.username)
        ).one()[0]
        record.news_topics = [topic.value for topic in topics]
        session.add(record)
        session.commit()
        session.refresh(record)
    return topics


@app.get(
    "/users/topics",
    responses={200: {"description": "The users topics"}},
    description="Gets the users current subscribed news topics",
)
async def get_users_topics(
    userSession: UserSession = Depends(get_headers),
) -> list[NewsTopics]:
    validateSession(userSession.username, userSession.session_id)

    with Session(engine) as session:
        record = session.execute(
            select(UserProfiles).where(UserProfiles.username == userSession.username)
        ).one()[0]

    return record.news_topics


# endregion USERS
@app.get(
    "/topics",
    responses={200: {"description": "The available News Topics"}},
    description="Gets all available News topics that a user can subscribe to",
)
async def get_topics() -> list[NewsTopics]:
    return [str(e.value) for e in NewsTopics]


@app.get(
    "/subscriptions",
    responses={200: {"description": "The available subscriptions"}},
    description="Gets all available space event topics that a user can subscribe to",
)
async def get_subscriptions() -> list[SubscriptionTopics]:
    return [str(e.value) for e in SubscriptionTopics]


if __name__ == "__main__":
    import uvicorn
    import yaml

    openapi = app.openapi()
    version = openapi.get("openapi", "version")
    with open("spec.yaml", "w") as f:
        yaml.dump(openapi, f, sort_keys=False)

    uvicorn.run(app, host="0.0.0.0", port=4343)
