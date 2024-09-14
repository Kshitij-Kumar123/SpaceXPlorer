package com.ece452.spacexplorer

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ece452.spacexplorer.networking.models.data.EventType
import com.ece452.spacexplorer.utils.AuthManager
import com.ece452.spacexplorer.utils.DataManager
import com.ece452.spacexplorer.utils.SessionIDManager
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

// These tests are for the DataManager API calls
@RunWith(AndroidJUnit4::class)
class DataInstrumentedTest {

    private val TAG = "DataInstrumentedTest"

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    // testing variables - every test should be a new username since we're doing the entire flow starting from register
    private val username = "data test" + UUID.randomUUID().toString()
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
        registerLatch.await(5, TimeUnit.SECONDS) // wait for registration to complete

        // ---------- Login ----------
        val loginLatch = CountDownLatch(1)
        Log.d(TAG, "Logging in")
        AuthManager.login(appContext, username, password) { success ->
            Log.d(TAG, "Login success: $success")
            assertTrue(success)
            loginLatch.countDown()
        }
        loginLatch.await(5, TimeUnit.SECONDS) // wait for login to complete

        val session_id = SessionIDManager.getSessionID(appContext).toString()
        DataManager.update_user_session(username, session_id) // update user session for instrument testing purposes

        // ---------- Get Events ----------
        Log.d(TAG, "Getting events")
        val getEventsLatch = CountDownLatch(1)
        val queryParams = listOf(EventType.NEO)
        DataManager.getEvents(queryParams) { success, events ->
            Log.d(TAG, "Get events success: $success")
            Log.d(TAG, "Events: $events")
            event_id = events?.get(0)?.event_id ?: "" // pick a random event to test comments later
            Log.d(TAG, "Event ID: $event_id")
            assertTrue(success)
            var like_status = events?.get(0)?.like_status
            Log.d(TAG, "like_status: $like_status")
            getEventsLatch.countDown()
        }
        getEventsLatch.await(5, TimeUnit.SECONDS)

        // ---------- Get User Events ----------
        Log.d(TAG, "Getting user events")
        val getUserEventsLatch = CountDownLatch(1)
        DataManager.getUserEvents() { success, events ->
            Log.d(TAG, "Get user events success: $success")
            Log.d(TAG, "User events: $events")
            assertTrue(success)
            getUserEventsLatch.countDown()
        }
        getUserEventsLatch.await(5, TimeUnit.SECONDS)

        // ---------- Get Event Comments ----------
        Log.d(TAG, "Getting event comments")
        val getEventCommentsLatch = CountDownLatch(1)
        DataManager.getEventComments(event_id) { success, comments ->
            Log.d(TAG, "Get event comments success: $success")
            Log.d(TAG, "Event comments: $comments")
            assertTrue(success)
            getEventCommentsLatch.countDown()
        }
        getEventCommentsLatch.await(5, TimeUnit.SECONDS)

        // ---------- Get News ----------
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

        // ---------- Get News Comments ----------
        Log.d(TAG, "Getting news comments")
        val getNewsCommentsLatch = CountDownLatch(1)
        DataManager.getNewsComments(article_id) { success, comments ->
            Log.d(TAG, "Get news comments success: $success")
            Log.d(TAG, "News comments: $comments")
            assertTrue(success)
            getNewsCommentsLatch.countDown()
        }
        getNewsCommentsLatch.await(5, TimeUnit.SECONDS)
    }
}
