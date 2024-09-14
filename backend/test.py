import logging
import random
import requests
import dotenv
import os

from data_microservice.API_Interfaces import main

VERSION = "434"
VERSION = "800"
AUTH_MICROSERVICE = f"http://localhost:{VERSION}1"
DATA_MICROSERVICE = f"http://localhost:{VERSION}2"
USER_MICROSERVICE = f"http://localhost:{VERSION}3"


numsuffix = random.randint(1, 100000)
logging.info(f"SEED {numsuffix}")
uname = f"TEST_{numsuffix}"


logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)

main(logger)

itemID = 2


r = requests.post(
    f"{AUTH_MICROSERVICE}/user/register_account",
    json={
        "username": "test2",
        "email": "TEST_@gmail.com",
        "password": "test",
        "phone_number": "123456789",
    },
)
assert r.status_code == 200


r = requests.post(
    f"{AUTH_MICROSERVICE}/user/register_account",
    json={
        "username": uname,
        "email": "TEST_@gmail.com",
        "password": "deodhgar",
        "phone_number": "123456789",
    },
)
assert r.status_code == 200
assert r.json()["username"] == uname


r = requests.post(
    f"{AUTH_MICROSERVICE}/user/login",
    json={"username": uname, "password": "deodhgar"},
)
assert r.status_code == 200
sessionID = r.json()["detail"]["session_id"]

import json

logging.info(json.dumps({"username": uname, "session_id": sessionID}))


# Data stuff


r = requests.get(
    f"{DATA_MICROSERVICE}/events/all",
    params={"filter": ["neo", "donki"]},
    headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
)


assert r.status_code == 200


result = r.json()
assert len(result) > 0
eventID = result[itemID]["event_id"]
eventID1 = result[-1]["event_id"]
# eventID = "b7c5cfb8-6bd3-42e3-8b06-4cfbc4524453"
logging.info("eventID", eventID, eventID1)


"""
Get default article ID
"""
# Sub to topics

r = requests.put(
    f"{USER_MICROSERVICE}/users/topics",
    headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    params={"topics": ["Planets", "NASA"]},
)
assert r.status_code == 200

r = requests.put(
    f"{USER_MICROSERVICE}/users/topics",
    headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    params={"topics": ["Asteroids"]},
)
assert r.status_code == 200


r = requests.get(
    f"{DATA_MICROSERVICE}/news",
    headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
)
print(r.json())
quit()
articleID = r.json()[itemID]["article_id"]
assert r.json()[itemID]["like_status"] == "neutral"


# Test Event likes!
def getEventByID(eventID):
    r = requests.get(
        f"{DATA_MICROSERVICE}/events/all",
        params={"filter": ["neo", "donki"]},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    for event in r.json():
        if event["event_id"] == eventID:
            return event
    assert False


def testEventLikes():
    logging.warning("Test Event Likes")
    # Like the event
    r = requests.put(
        f"{USER_MICROSERVICE}/events/likes/like",
        params={"event_id": eventID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()["likes"] == 1

    # Check that the event was liked, and status is updated
    r = getEventByID(eventID)
    assert r["like_status"] == "like"
    assert r["likes"] == 1
    assert r["dislikes"] == 0

    # Unlike the event
    r = requests.put(
        f"{USER_MICROSERVICE}/events/likes/unlike",
        params={"event_id": eventID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()["likes"] == 0

    # Check that the even was unlniked
    r = getEventByID(eventID)

    assert r["like_status"] == "neutral"
    assert r["likes"] == 0
    assert r["dislikes"] == 0

    # Dislike the event
    r = requests.put(
        f"{USER_MICROSERVICE}/events/likes/dislike",
        params={"event_id": eventID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()["dislikes"] == 1

    # Check that the even was unlniked
    r = getEventByID(eventID)
    assert r["like_status"] == "dislike"
    assert r["likes"] == 0
    assert r["dislikes"] == 1

    # undislike the event
    r = requests.put(
        f"{USER_MICROSERVICE}/events/likes/undislike",
        params={"event_id": eventID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()["dislikes"] == 0

    # Check that the even was unlniked
    r = getEventByID(eventID)
    assert r["like_status"] == "neutral"
    assert r["likes"] == 0
    assert r["dislikes"] == 0


def getEventCommentByID(eventID, commentID):
    r = requests.get(
        f"{DATA_MICROSERVICE}/events/comments",
        params={"event_id": eventID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )

    assert r.status_code == 200

    for comment in r.json():
        if comment["comment_id"] == commentID:
            return comment
    assert False


def testEventCommentLikes():
    logging.warning("Test Event Likes")
    # Comment on the event!
    r = requests.put(
        f"{USER_MICROSERVICE}/events/comments",
        params={"event_id": eventID, "comment": f"This is a test {numsuffix}"},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    eventCommentID = r.json()["comment_id"]
    logging.warning(f"Testing with comment ID {eventCommentID}")

    # Check comment_count
    r = getEventByID(eventID)
    assert r["comment_count"] == 1

    # Like the comment
    r = requests.put(
        f"{USER_MICROSERVICE}/events/comments/likes/like",
        params={"event_id": eventID, "comment_id": eventCommentID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()["likes"] == 1

    # Check that the comment was liked, and status is updated
    r = getEventCommentByID(eventID, eventCommentID)
    assert r["like_status"] == "like"
    assert r["likes"] == 1
    assert r["dislikes"] == 0

    # Unlike the comment
    r = requests.put(
        f"{USER_MICROSERVICE}/events/comments/likes/unlike",
        params={"event_id": eventID, "comment_id": eventCommentID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()["likes"] == 0

    # Check that the comment was unlniked
    r = getEventCommentByID(eventID, eventCommentID)
    assert r["like_status"] == "neutral"
    assert r["likes"] == 0
    assert r["dislikes"] == 0

    # Dislike the event
    r = requests.put(
        f"{USER_MICROSERVICE}/events/comments/likes/dislike",
        params={"event_id": eventID, "comment_id": eventCommentID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()["dislikes"] == 1

    # Check that the even was unlniked
    r = getEventCommentByID(eventID, eventCommentID)
    assert r["like_status"] == "dislike"
    assert r["likes"] == 0
    assert r["dislikes"] == 1

    # undislike the event
    r = requests.put(
        f"{USER_MICROSERVICE}/events/comments/likes/undislike",
        params={"event_id": eventID, "comment_id": eventCommentID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()["dislikes"] == 0

    # Check that the even was unlniked
    r = getEventCommentByID(eventID, eventCommentID)
    assert r["like_status"] == "neutral"
    assert r["likes"] == 0
    assert r["dislikes"] == 0


"""

NEWS!!!


"""


def getArticleByID(articleID):
    r = requests.get(
        f"{DATA_MICROSERVICE}/news",
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    for article in r.json():
        if article["article_id"] == articleID:
            return article
    assert False


def testArticleLikes():
    logging.warning("Test Article Likes")
    logging.warning(f"ArticleID {articleID}")
    # Like the event
    r = requests.put(
        f"{USER_MICROSERVICE}/news/likes/like",
        params={"article_id": articleID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()["likes"] == 1

    # Check that the event was liked, and status is updated
    r = getArticleByID(articleID)
    assert r["like_status"] == "like"
    assert r["likes"] == 1
    assert r["dislikes"] == 0

    # Unlike the event
    r = requests.put(
        f"{USER_MICROSERVICE}/news/likes/unlike",
        params={"article_id": articleID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()["likes"] == 0

    # Check that the even was unlniked
    r = getArticleByID(articleID)

    assert r["like_status"] == "neutral"
    assert r["likes"] == 0
    assert r["dislikes"] == 0

    # Dislike the event
    r = requests.put(
        f"{USER_MICROSERVICE}/news/likes/dislike",
        params={"article_id": articleID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()["dislikes"] == 1

    # Check that the even was unlniked
    r = getArticleByID(articleID)
    assert r["like_status"] == "dislike"
    assert r["likes"] == 0
    assert r["dislikes"] == 1

    # undislike the event
    r = requests.put(
        f"{USER_MICROSERVICE}/news/likes/undislike",
        params={"article_id": articleID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()["dislikes"] == 0

    # Check that the even was unlniked
    r = getArticleByID(articleID)
    assert r["like_status"] == "neutral"
    assert r["likes"] == 0
    assert r["dislikes"] == 0


def getNewsCommentByID(articleID, commentID):
    r = requests.get(
        f"{DATA_MICROSERVICE}/news/comments",
        params={"article_id": articleID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )

    assert r.status_code == 200

    for comment in r.json():
        if comment["comment_id"] == commentID:
            return comment
    assert False


def testNewsCommentLikes():
    logging.warning("Testing news comment likes")
    # Comment on the article!
    r = requests.put(
        f"{USER_MICROSERVICE}/news/comments",
        params={"article_id": articleID, "comment": f"This is a test {numsuffix}"},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )

    assert r.status_code == 200
    newsCommentID = r.json()["comment_id"]
    logging.info(f"Testing with comment ID {newsCommentID}")

    r = getArticleByID(articleID)
    assert r["comment_count"] == 1

    # Like the comment
    r = requests.put(
        f"{USER_MICROSERVICE}/news/comments/likes/like",
        params={"article_id": articleID, "comment_id": newsCommentID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()["likes"] == 1

    # Check that the comment was liked, and status is updated
    r = getNewsCommentByID(articleID, newsCommentID)
    assert r["like_status"] == "like"
    assert r["likes"] == 1
    assert r["dislikes"] == 0

    # # Unlike the comment
    # r = requests.put(
    #     f"{USER_MICROSERVICE}/news/comments/likes/unlike",
    #     params={"article_id": articleID, "comment_id": newsCommentID},
    #     headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    # )
    # assert r.status_code == 200
    # assert r.json()["likes"] == 0
    #
    # # Check that the comment was unlniked
    # r = getNewsCommentByID(articleID, newsCommentID)
    # assert r["like_status"] == "neutral"
    # assert r["likes"] == 0
    # assert r["dislikes"] == 0

    # Dislike the article
    r = requests.put(
        f"{USER_MICROSERVICE}/news/comments/likes/dislike",
        params={"article_id": articleID, "comment_id": newsCommentID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()["dislikes"] == 1
    assert r.json()["likes"] == 0

    # Check that the article was unlniked
    r = getNewsCommentByID(articleID, newsCommentID)
    assert r["like_status"] == "dislike"
    assert r["likes"] == 0
    assert r["dislikes"] == 1

    # undislike the article
    r = requests.put(
        f"{USER_MICROSERVICE}/news/comments/likes/undislike",
        params={"article_id": articleID, "comment_id": newsCommentID},
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()["dislikes"] == 0

    # Check that the even was unlniked
    r = getNewsCommentByID(articleID, newsCommentID)
    assert r["like_status"] == "neutral"
    assert r["likes"] == 0
    assert r["dislikes"] == 0


def testGetSubscriptions():
    r = requests.get(
        f"{USER_MICROSERVICE}/subscriptions",
    )
    assert r.status_code == 200
    assert r.json() == ["solar", "donki", "launch", "neo"]


def testGetTopics():
    r = requests.get(
        f"{USER_MICROSERVICE}/topics",
    )
    assert r.status_code == 200
    assert r.json() == [
        "Planets",
        "Eclipses",
        "NASA",
        "Comets",
        "Asteroids",
        "Black-holes",
    ]


def testLoginLogout():
    userProfile = {
        "username": f"THIS IS A TEST{random.randint(0,999)}",
        "email": f"THIS IS A TEST{random.randint(0,999)}",
        "password": f"THIS IS A TEST{random.randint(0,999)}",
        "phone_number": f"THIS IS A TEST{random.randint(0,999)}",
    }

    r = requests.post(
        f"{AUTH_MICROSERVICE}/user/register_account",
        json=userProfile,
    )
    assert r.status_code == 200
    assert {key: r.json()[key] for key in userProfile.keys()} == userProfile

    r = requests.post(
        f"{AUTH_MICROSERVICE}/user/login",
        json={"username": userProfile["username"], "password": userProfile["password"]},
    )
    assert r.status_code == 200
    sessionID = r.json()["detail"]["session_id"]

    r = requests.get(
        f"{AUTH_MICROSERVICE}/user/profile",
        headers={"Auth-Username": userProfile["username"], "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert {key: r.json()["detail"][key] for key in userProfile.keys()} == userProfile

    r = requests.post(
        f"{AUTH_MICROSERVICE}/user/logout",
        headers={"Auth-Username": userProfile["username"], "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200

    # Check that user is logged out correctly
    r = requests.get(
        f"{AUTH_MICROSERVICE}/user/profile",
        headers={"Auth-Username": userProfile["username"], "Auth-SessionID": sessionID},
    )
    assert r.status_code == 400


# testLoginLogout()


def testLoginLogout():
    userProfile = {
        "username": f"THIS IS A TEST{random.randint(0,999)}",
        "email": f"THIS IS A TEST{random.randint(0,999)}",
        "password": f"THIS IS A TEST{random.randint(0,999)}",
        "phone_number": f"THIS IS A TEST{random.randint(0,999)}",
    }

    newUserProfile = {
        "username": f"THIS IS A NEW TEST{random.randint(0,999)}",
        "email": f"THIS IS A NEW TEST{random.randint(0,999)}",
        "password": f"THIS IS A NEW TEST{random.randint(0,999)}",
        "phone_number": f"THIS IS A NEW TEST{random.randint(0,999)}",
    }

    r = requests.post(
        f"{AUTH_MICROSERVICE}/user/register_account",
        json=userProfile,
    )
    assert r.status_code == 200
    assert {key: r.json()[key] for key in userProfile.keys()} == userProfile

    r = requests.post(
        f"{AUTH_MICROSERVICE}/user/login",
        json={"username": userProfile["username"], "password": userProfile["password"]},
    )
    assert r.status_code == 200
    sessionID = r.json()["detail"]["session_id"]

    r = requests.get(
        f"{AUTH_MICROSERVICE}/user/profile",
        headers={"Auth-Username": userProfile["username"], "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert {key: r.json()["detail"][key] for key in userProfile.keys()} == userProfile

    r = requests.post(
        f"{AUTH_MICROSERVICE}/user/update_account",
        json=newUserProfile,
        headers={"Auth-Username": userProfile["username"], "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert {key: r.json()[key] for key in newUserProfile.keys()} == newUserProfile

    r = requests.get(
        f"{AUTH_MICROSERVICE}/user/profile",
        headers={
            "Auth-Username": newUserProfile["username"],
            "Auth-SessionID": sessionID,
        },
    )
    assert r.status_code == 200
    assert {
        key: r.json()["detail"][key] for key in newUserProfile.keys()
    } == newUserProfile

    r = requests.post(
        f"{AUTH_MICROSERVICE}/user/logout",
        headers={
            "Auth-Username": newUserProfile["username"],
            "Auth-SessionID": sessionID,
        },
    )
    assert r.status_code == 200

    # Check that user is logged out correctly
    r = requests.get(
        f"{AUTH_MICROSERVICE}/user/profile",
        headers={
            "Auth-Username": newUserProfile["username"],
            "Auth-SessionID": sessionID,
        },
    )
    assert r.status_code == 400

    # Make sure that original user cannot log back in
    r = requests.post(
        f"{AUTH_MICROSERVICE}/user/login",
        json={"username": userProfile["username"], "password": userProfile["password"]},
    )
    assert r.status_code == 400

    # make sure that user can log in with new ID
    r = requests.post(
        f"{AUTH_MICROSERVICE}/user/login",
        json={
            "username": newUserProfile["username"],
            "password": newUserProfile["password"],
        },
    )
    assert r.status_code == 200


def testGoogleAPI():
    dotenv.load_dotenv(".env")

    r = requests.get(
        f"{AUTH_MICROSERVICE}/apis/google",
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json() == os.getenv("GOOGLE_API")


def testEventSubscriptions():
    r = requests.get(
        f"{USER_MICROSERVICE}/users/subscriptions",
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert len(r.json()) == 0

    # Check specific endpoints
    r = requests.get(
        f"{DATA_MICROSERVICE}/events/user",
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert len(r.json()) == 0

    r = requests.put(
        f"{USER_MICROSERVICE}/users/subscriptions",
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
        params={"event_id": eventID},
    )
    assert r.status_code == 200
    assert r.json() == [eventID]

    r = requests.get(
        f"{USER_MICROSERVICE}/users/subscriptions",
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json() == [eventID]

    r = requests.get(
        f"{DATA_MICROSERVICE}/events/user",
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()[0]["event_id"] == eventID

    r = requests.put(
        f"{USER_MICROSERVICE}/users/subscriptions",
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
        params={"event_id": eventID1},
    )
    assert r.status_code == 200

    r = requests.get(
        f"{USER_MICROSERVICE}/users/subscriptions",
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert set(r.json()) == {eventID, eventID1}

    r = requests.get(
        f"{DATA_MICROSERVICE}/events/user",
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert set([x["event_id"] for x in r.json()]) == {eventID, eventID1}

    r = requests.delete(
        f"{USER_MICROSERVICE}/users/subscriptions",
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
        params={"event_id": eventID},
    )
    assert r.status_code == 200
    assert r.json() == [eventID1]

    r = requests.get(
        f"{DATA_MICROSERVICE}/events/user",
        headers={"Auth-Username": uname, "Auth-SessionID": sessionID},
    )
    assert r.status_code == 200
    assert r.json()[0]["event_id"] == eventID1


"""













r = requests.put(
    f"{USER_MICROSERVICE}/events/comments/likes/like",
    params={"event_id": eventID, "comment_id": eventCommentID},
    json={"username": uname, "session_id": sessionID},
)
assert r.status_code == 200


r = requests.put(
    f"{USER_MICROSERVICE}/news/comments",
    params={"article_id": articleID, "comment": f"This is a test {numsuffix}"},
    json={"username": uname, "session_id": sessionID},
)
assert r.status_code == 200
logging.info(r.json())


r = requests.post(
    f"{DATA_MICROSERVICE}/news/comments",
    params={"article_id": articleID, "comment": f"This is a test {numsuffix}"},
    json={"username": uname, "session_id": sessionID},
)


assert r.status_code == 200
logging.info(r.json())


r = requests.post(
    f"{DATA_MICROSERVICE}/events/comments",
    params={"event_id": eventID},
    json={"username": uname, "session_id": sessionID},
)


assert r.status_code == 200
logging.info(r.json())


# likes
logging.info("start test")
r = requests.post(
    f"{DATA_MICROSERVICE}/events/all",
    json={"username": uname, "session_id": sessionID},
    params={"filter": ["neo", "donki"]},
)
assert r.status_code == 200
logging.info(r.json())


# Like an event and make sure that when we fetch again it shows up as liked
r = requests.put(
    f"{USER_MICROSERVICE}/events/likes/like",
    params={"event_id": eventID},
    json={"username": uname, "session_id": sessionID},
)
assert r.status_code == 200
logging.info(r.json())


r = requests.post(
    f"{DATA_MICROSERVICE}/events/all",
    json={"username": uname, "session_id": sessionID},
    params={"filter": ["neo", "donki"]},
)
assert r.status_code == 200
logging.info(r.json())
assert r.json()[0]
logging.info("end test")
# End data stuff!



"""
