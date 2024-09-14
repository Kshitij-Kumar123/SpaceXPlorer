import yaml
import logging

from sqlalchemy import create_engine
from sqlalchemy.exc import IntegrityError
from gen_utils.general_API_classes import (
    UserCredentials,
    UserSession,
    UserProfiles,
    get_headers,
)
from gen_utils.utils import *
from fastapi import FastAPI, status, HTTPException, Query, Depends
from API_classes import *
from contextlib import asynccontextmanager
from gen_utils.load_envs import DATABASE_URL, GOOGLE_API
from psycopg2.errors import UniqueViolation
from sqlmodel import select, SQLModel, Session

logger = logging.getLogger("uvicorn.error")
logger.setLevel(logging.INFO)

engine = create_engine(DATABASE_URL, echo=False)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("init database!")
    SQLModel.metadata.create_all(engine, checkfirst=True)
    logger.info("Initializing database")
    yield


app = FastAPI(title="User Authentication Microservice", lifespan=lifespan)


@app.post(
    "/user/register_account",
    responses={
        status.HTTP_200_OK: {"description": "Item created successfully"},
        status.HTTP_409_CONFLICT: {"description": "Account / Username already exists!"},
        status.HTTP_422_UNPROCESSABLE_ENTITY: {
            "description": "Error processing entity"
        },
    },
)
async def post_user_register_account(
    registerBody: UserProfiles,
) -> UserProfiles:
    try:
        with Session(engine) as session:
            session.add(registerBody)
            session.commit()
            session.refresh(registerBody)
    except IntegrityError as integrityError:
        session.rollback()
        if isinstance(integrityError.orig, UniqueViolation):
            logger.warning(
                f"Attempted to create duplicate account with username: {registerBody.username}"
            )
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Username / Account already exists!",
            )
        else:
            raise integrityError
    print(registerBody)
    return registerBody


@app.post(
    "/user/login",
    responses={
        status.HTTP_200_OK: {"description": "Successfully logged in"},
        status.HTTP_400_BAD_REQUEST: {"description": "Incorrect username or password!"},
        status.HTTP_422_UNPROCESSABLE_ENTITY: {
            "description": "Error processing entity"
        },
    },
)
async def post_user_login(userCreds: UserCredentials) -> LoginBodyReturn:
    try:
        with Session(engine) as session:
            record = session.execute(
                select(UserProfiles).where(UserProfiles.username == userCreds.username)
            ).fetchone()

        if record is None or record[0].password != userCreds.password:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Incorrect username or password!",
            )

    except Exception as e:
        raise e

    sessionID = uuid.uuid4()
    redisConnection = createRedisConnection()
    redisConnection.set(userCreds.username, str(sessionID))

    return LoginBodyReturn(
        message="Logged in Successfully",
        detail=LoginBodyDetails(username=userCreds.username, session_id=sessionID),
    )


@app.post(
    "/user/logout",
    responses={
        status.HTTP_200_OK: {"description": "Successfully logged out"},
        status.HTTP_400_BAD_REQUEST: {
            "description": "User does not exist or is not logged in!"
        },
        status.HTTP_422_UNPROCESSABLE_ENTITY: {
            "description": "Error processing entity"
        },
    },
)
async def post_user_logout(
    userSession: UserSession = Depends(get_headers),
) -> LogoutBodyReturn:
    redisConnection = createRedisConnection()
    validateSession(userSession.username, userSession.session_id)
    redisConnection.delete(userSession.username)
    return LogoutBodyReturn(
        message="Successfully logged out",
        detail=LogoutBodyDetails(
            username=userSession.username, session_id=userSession.session_id
        ),
    )


@app.get(
    "/user/profile",
    responses={
        status.HTTP_200_OK: {"description": "Successfully retrieved User Profile"},
        status.HTTP_400_BAD_REQUEST: {
            "description": "User does not exist or is not logged in!"
        },
        status.HTTP_422_UNPROCESSABLE_ENTITY: {
            "description": "Error processing entity"
        },
    },
)
async def get_user_profile(
    userSession: UserSession = Depends(get_headers),
) -> ProfileBodyReturn:
    validateSession(userSession.username, userSession.session_id)

    with Session(engine) as session:
        record = session.execute(
            select(UserProfiles).where(UserProfiles.username == userSession.username)
        ).fetchone()

    if record is None:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Incorrect username",
        )

    return ProfileBodyReturn(
        message="Successfully retrieved user information",
        detail=ProfileBodyDetails(
            username=record[0].username,
            email=record[0].email,
            password=record[0].password,
            phone_number=record[0].phone_number,
        ),
    )


@app.post(
    "/user/update_account",
    responses={
        status.HTTP_200_OK: {"description": "Item updated successfully"},
        status.HTTP_409_CONFLICT: {"description": "Account / Username already exists!"},
        status.HTTP_422_UNPROCESSABLE_ENTITY: {
            "description": "Error processing entity"
        },
    },
)
async def post_user_update_account(
    userProfile: UserProfiles, userSession: UserSession = Depends(get_headers)
) -> UserProfiles:
    validateSession(userSession.username, userSession.session_id)

    with Session(engine) as session:
        record = session.execute(
            select(UserProfiles).where(UserProfiles.username == userSession.username)
        ).one()[0]

        record.username = userProfile.username
        record.email = userProfile.email
        record.password = userProfile.password
        record.phone_number = userProfile.phone_number

        redisConnection = createRedisConnection()
        sessionID = redisConnection.get(userSession.username)
        redisConnection.delete(userSession.username)
        redisConnection.set(userProfile.username, str(sessionID))

        session.add(record)
        session.commit()

    return userProfile


@app.get(
    "/apis/google",
    responses={
        status.HTTP_200_OK: {"description": "Returned Google API Key"},
    },
)
async def get_apis_google(userSession: UserSession = Depends(get_headers)) -> str:
    validateSession(userSession.username, userSession.session_id)
    return GOOGLE_API


if __name__ == "__main__":
    import uvicorn

    openapi = app.openapi()
    version = openapi.get("openapi", "version")
    with open("spec.yaml", "w") as f:
        yaml.dump(openapi, f, sort_keys=False)

    uvicorn.run(app, host="0.0.0.0", port=4341)
