package com.ece452.spacexplorer

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ece452.spacexplorer.networking.models.data.EventType
import com.ece452.spacexplorer.networking.models.userinteractions.LikesDislikesType
import com.ece452.spacexplorer.networking.models.userinteractions.TopicsType
import com.ece452.spacexplorer.utils.AuthManager
import com.ece452.spacexplorer.utils.DataManager
import com.ece452.spacexplorer.utils.SessionIDManager
import com.ece452.spacexplorer.utils.UserInteractionsManager
import java.util.UUID

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

// These tests are for the UserInteractionsManager API calls
@RunWith(AndroidJUnit4::class)
class UserInteractionsInstrumentedTest {

    private val TAG = "UserInteractionsInstrumentedTest"

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    // testing variables - every test should be a new username since we're doing the entire flow starting from register
    private val username = "userinteractions test"  + UUID.randomUUID().toString()
    private val email = "test@test.com"
    private val phone_number = "1234567890"
    private val password = "Test#1234"
    private val confirm_password = "Test#1234"
    private var event_id = "" // will be picked later based on the events fetched
    private var article_id = "" // will be picked later based on the news fetched

    @Test
    fun main() {
        // ---------- Register ----------
        val registerLatch = CountDownLatch(1)
        Log.d(TAG, "Registering account")
        AuthManager.register(appContext, username, email, phone_number, password, confirm_password) { success ->
            Log.d(TAG, "Register success: $success")
            assertTrue(success)
            registerLatch.countDown() // decrease count by 1
        }
        registerLatch.await(5, TimeUnit.SECONDS)

        // ---------- Login ----------
        val loginLatch = CountDownLatch(1)
        Log.d(TAG, "Logging in")
        AuthManager.login(appContext, username, password) { success ->
            Log.d(TAG, "Login success: $success")
            assertTrue(success)
            loginLatch.countDown()
        }
        loginLatch.await(5, TimeUnit.SECONDS)

        val session_id = SessionIDManager.getSessionID(appContext).toString()
        UserInteractionsManager.update_user_session(username, session_id) // update user session for instrument testing purposes
        DataManager.update_user_session(username, session_id) // this one is purely for the functions to populate event_id and article_id

        // ---------- Get Events ---------- (purely for event_id, this function is the same as in DataInstrumentedTest)
        Log.d(TAG, "Getting events")
        val getEventsLatch = CountDownLatch(1)
        val queryParams = listOf(EventType.NEO)
        DataManager.getEvents(queryParams) { success, events ->
            Log.d(TAG, "Get events success: $success")
            Log.d(TAG, "Events: $events")
            event_id = events?.get(0)?.event_id ?: "" // pick a random event to test comments later
            Log.d(TAG, "Event ID: $event_id")
            assertTrue(success)
            getEventsLatch.countDown()
        }
        getEventsLatch.await(5, TimeUnit.SECONDS)

        // ---------- Put Event Like ----------
        putEventLike(LikesDislikesType.LIKE, event_id)
        putEventLike(LikesDislikesType.DISLIKE, event_id)
        putEventLike(LikesDislikesType.UNLIKE, event_id)
        putEventLike(LikesDislikesType.UNDISLIKE, event_id)

        // ---------- Put Event Comment ----------
        var comment_id: String = ""
        val putEventCommentLatch = CountDownLatch(1)
        Log.d(TAG, "Putting event comment")
        UserInteractionsManager.putEventComment(event_id, "joel should shave his head") { success, response ->
            Log.d(TAG, "Put event comment success: $success")
            Log.d(TAG, "Put event comment response: $response")
            comment_id = response?.comment_id ?: ""
            Log.d(TAG, "Comment ID: $comment_id")
            assertTrue(success)
            putEventCommentLatch.countDown()
        }
        putEventCommentLatch.await(5, TimeUnit.SECONDS)

        // ---------- Put Event Comment Like ----------
        putEventCommentLike(LikesDislikesType.LIKE, event_id, comment_id)
        putEventCommentLike(LikesDislikesType.DISLIKE, event_id, comment_id)
        putEventCommentLike(LikesDislikesType.UNLIKE, event_id, comment_id)
        putEventCommentLike(LikesDislikesType.UNDISLIKE, event_id, comment_id)

        // ---------- Get News ---------- (purely for article_id, this function is the same as in DataInstrumentedTest)
        Log.d(TAG, "Getting news")
        val getNewsLatch = CountDownLatch(1)
        DataManager.getNews() { success, news ->
            Log.d(TAG, "Get news success: $success")
            Log.d(TAG, "News: $news")
            article_id = news?.get(0)?.article_id ?: "" // pick a random article to test comments later
            Log.d(TAG, "Article ID: $article_id")
            assertTrue(success)
            getNewsLatch.countDown()
        }
        getNewsLatch.await(5, TimeUnit.SECONDS)

        // ---------- Put News Like ----------
        putNewsLike(LikesDislikesType.LIKE, article_id)
        putNewsLike(LikesDislikesType.DISLIKE, article_id)
        putNewsLike(LikesDislikesType.UNLIKE, article_id)
        putNewsLike(LikesDislikesType.UNDISLIKE, article_id)

        // ---------- Put News Comment ----------
        val putNewsCommentLatch = CountDownLatch(1)
        Log.d(TAG, "Putting news comment")
        UserInteractionsManager.putNewsComment(article_id, "joel 'tim cook' deodhar") { success, response ->
            Log.d(TAG, "Put news comment success: $success")
            Log.d(TAG, "Put news comment response: $response")
            comment_id = response?.comment_id ?: ""
            Log.d(TAG, "Comment ID: $comment_id")
            assertTrue(success)
            putNewsCommentLatch.countDown()
        }
        putNewsCommentLatch.await(5, TimeUnit.SECONDS)

        // ---------- Put News Comment Like ----------
        putNewsCommentLike(LikesDislikesType.LIKE, article_id, comment_id)
        putNewsCommentLike(LikesDislikesType.DISLIKE, article_id, comment_id)
        putNewsCommentLike(LikesDislikesType.UNLIKE, article_id, comment_id)
        putNewsCommentLike(LikesDislikesType.UNDISLIKE, article_id, comment_id)

        // ---------- Get Subscriptions ----------
        val getSubscriptionsLatch = CountDownLatch(1)
        Log.d(TAG, "Getting subscriptions")
        UserInteractionsManager.getSubscriptions() { success, response ->
            Log.d(TAG, "Get subscriptions success: $success")
            Log.d(TAG, "Get subscriptions response: $response")
            assertTrue(success)
            getSubscriptionsLatch.countDown()
        }
        getSubscriptionsLatch.await(5, TimeUnit.SECONDS)

        // ---------- Get Topics ----------
        val getTopicsLatch = CountDownLatch(1)
        Log.d(TAG, "Getting topics")
        UserInteractionsManager.getTopics() { success, response ->
            Log.d(TAG, "Get topics success: $success")
            Log.d(TAG, "Get topics response: $response")
            assertTrue(success)
            getTopicsLatch.countDown()
        }
        getTopicsLatch.await(5, TimeUnit.SECONDS)

        // ---------- Put User Subscriptions ----------
        val putUserSubscriptionsLatch = CountDownLatch(1)
        Log.d(TAG, "Putting user subscriptions")
        UserInteractionsManager.putUserSubscriptions(event_id) { success, response ->
            Log.d(TAG, "Put user subscriptions success: $success")
            Log.d(TAG, "Put user subscriptions response: $response")
            assertTrue(success)
            putUserSubscriptionsLatch.countDown()
        }
        putUserSubscriptionsLatch.await(5, TimeUnit.SECONDS)

        // ---------- Get User Subscription ----------
        val getUserSubscriptionLatch = CountDownLatch(1)
        Log.d(TAG, "Getting user subscription")
        UserInteractionsManager.getUserSubscription() { success, response ->
            Log.d(TAG, "Get user subscription success: $success")
            Log.d(TAG, "Get user subscription response: $response")
            assertTrue(success)
            getUserSubscriptionLatch.countDown()
        }
        getUserSubscriptionLatch.await(5, TimeUnit.SECONDS)

        // ---------- Delete User Subscription ----------
        val deleteUserSubscriptionLatch = CountDownLatch(1)
        Log.d(TAG, "Deleting user subscription")
        UserInteractionsManager.deleteUserSubscription(event_id) { success, response ->
            Log.d(TAG, "Delete user subscription success: $success")
            Log.d(TAG, "Delete user subscription response: $response")
            assertTrue(success)
            deleteUserSubscriptionLatch.countDown()
        }
        deleteUserSubscriptionLatch.await(5, TimeUnit.SECONDS)

        // ---------- Put User Topics ----------
        val putUserTopicsLatch = CountDownLatch(1)
        Log.d(TAG, "Putting user topics")
        val new_topics: List<TopicsType> = listOf(TopicsType.PLANETS, TopicsType.ECLIPSES)
        UserInteractionsManager.putUserTopics(new_topics) { success, response ->
            Log.d(TAG, "Put user topics success: $success")
            Log.d(TAG, "Put user topics response: $response")
            assertTrue(success)
            putUserTopicsLatch.countDown()
        }
        putUserTopicsLatch.await(5, TimeUnit.SECONDS)

        // ---------- Get User Topics ----------
        val getUserTopicsLatch = CountDownLatch(1)
        Log.d(TAG, "Getting user topics")
        UserInteractionsManager.getUserTopics() { success, response ->
            Log.d(TAG, "Get user topics success: $success")
            Log.d(TAG, "Get user topics response: $response")
            assertTrue(success)
            getUserTopicsLatch.countDown()
        }
        getUserTopicsLatch.await(5, TimeUnit.SECONDS)
    }

    private fun putEventLike(action: LikesDislikesType, event_id: String) {
        val latch = CountDownLatch(1)
        Log.d(TAG, "Putting event like for action: $action")
        UserInteractionsManager.putEventLike(action, event_id) { success, response ->
            Log.d(TAG, "Put event like success: $success")
            Log.d(TAG, "Put event like response: $response")
            assertTrue(success)
            latch.countDown()
        }
        latch.await(5, TimeUnit.SECONDS)
    }

    private fun putEventCommentLike(action: LikesDislikesType, event_id: String, comment_id: String) {
        val latch = CountDownLatch(1)
        Log.d(TAG, "Putting event comment like for action: $action")
        UserInteractionsManager.putEventCommentLike(action, event_id, comment_id) { success, response ->
            Log.d(TAG, "Put event comment like success: $success")
            Log.d(TAG, "Put event comment like response: $response")
            assertTrue(success)
            latch.countDown()
        }
        latch.await(5, TimeUnit.SECONDS)
    }

    private fun putNewsLike(action: LikesDislikesType, article_id: String) {
        val latch = CountDownLatch(1)
        Log.d(TAG, "Putting news like for article: $article_id")
        UserInteractionsManager.putNewsLike(action, article_id) { success, response ->
            Log.d(TAG, "Put news like success: $success")
            Log.d(TAG, "Put news like response: $response")
            assertTrue(success)
            latch.countDown()
        }
        latch.await(5, TimeUnit.SECONDS)
    }

    private fun putNewsCommentLike(action: LikesDislikesType, article_id: String, comment_id: String) {
        val latch = CountDownLatch(1)
        Log.d(TAG, "Putting news comment like for action: $action")
        UserInteractionsManager.putNewsCommentLike(action, article_id, comment_id) { success, response ->
            Log.d(TAG, "Put news comment like success: $success")
            Log.d(TAG, "Put news comment like response: $response")
            assertTrue(success)
            latch.countDown()
        }
        latch.await(5, TimeUnit.SECONDS)
    }
}