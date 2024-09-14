import logging
import os
import dotenv

# Load from file system for non container development
if os.path.exists(os.path.join(os.path.dirname(__file__), "../.env")):
    logging.info("Loading from file!")
    dotenv.load_dotenv(os.path.join(os.path.dirname(__file__), "../.env"))


POSTGRES_PORT = os.getenv("POSTGRES_PORT")
POSTGRES_USER = os.getenv("POSTGRES_USER")
POSTGRES_PASSWORD = os.getenv("POSTGRES_PASSWORD")
POSTGRES_DBNAME = os.getenv("POSTGRES_DBNAME")
POSTGRES_HOST = os.getenv("POSTGRES_HOST")


REDIS_HOST = os.getenv("REDIS_HOST")
REDIS_PORT = os.getenv("REDIS_PORT")

NASA_API = os.getenv("NASA_API")
NEWS_API = os.getenv("NEWS_API")
CACHE = bool(int(os.getenv("CACHE")))
DATABASE_URL = f"postgresql://{POSTGRES_HOST}:{POSTGRES_PORT}/{POSTGRES_DBNAME}?user={POSTGRES_USER}&password={POSTGRES_PASSWORD}"

GOOGLE_API = os.getenv("GOOGLE_API")
