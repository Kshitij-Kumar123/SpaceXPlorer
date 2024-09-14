import psycopg2
import redis
from gen_utils import load_envs as envVars
from fastapi import FastAPI, status, HTTPException


def createDBConnection():
    return psycopg2.connect(
        user=envVars.POSTGRES_USER,
        password=envVars.POSTGRES_PASSWORD,
        host=envVars.POSTGRES_HOST,
        port=envVars.POSTGRES_PORT,
        database=envVars.POSTGRES_DBNAME,
    )


def createRedisConnection() -> redis.Redis:
    return redis.Redis(
        host=envVars.REDIS_HOST, port=envVars.REDIS_PORT, decode_responses=True
    )


def validateSession(username: str, userSessionID: str):
    redisConnection = createRedisConnection()
    sessionID = redisConnection.get(username)
    if sessionID is None or sessionID == False:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="User does not exist or is not logged in!",
        )
    return True
