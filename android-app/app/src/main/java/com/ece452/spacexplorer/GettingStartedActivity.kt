package com.ece452.spacexplorer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.util.Log // Logcat Logging
import androidx.appcompat.app.AppCompatActivity
import com.ece452.spacexplorer.utils.*

class GettingStartedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gettingstarted)

        // These managers need context to access saved username and session_id so we don't need to explicitly pass them all the time
        UserInteractionsManager.init(this)
        DataManager.init(this)
        
        // If the user is already logged in, skip the login/register pages and go straight to the main activity
        if (AuthManager.isLoggedIn(this@GettingStartedActivity)){
            Log.d("GettingStartedActivity", "User is already logged in")
            val intent = Intent(
                this@GettingStartedActivity,
                MainActivity::class.java
            )
            startActivity(intent)
        }

        val loginButton = findViewById<Button>(R.id.login_button)
        val registerButton = findViewById<Button>(R.id.register_button)

        // Switch to the login activity when the login button is clicked
        loginButton.setOnClickListener {
            val intent = Intent(
                this@GettingStartedActivity,
                LoginActivity::class.java
            )
            startActivity(intent)
        }

        // Switch to the register activity when the register button is clicked
        registerButton.setOnClickListener { // Navigate to RegisterActivity on click
            val intent = Intent(
                this@GettingStartedActivity,
                RegisterActivity::class.java
            )
            startActivity(intent)
        }

        // -------------------- Placeholder Example API calls for testing --------------------

        // -------------------- DATA API MICROSERVICE --------------------

        // testing variables
//        val username: String = "bruh"
//        val session_id: String = "fda91a2e-1c74-49e1-8496-12cca316bbc0"
//        val event_id: String = "2c86c541-c8f0-42b5-be91-70b26bbaa646"
//        val article_id: String = "70282fd9-e479-49fe-85bb-35601f6532e6"
//        val comment_id: String = "65158839-020b-45a9-be7f-3783abc63eb1" // "yah yah yeet comment"
//        val queryParams = listOf(EventType.NEO)

        // works
//         Gets all events according to the filters (EventType.NEO, EventType.DONKI, EventType.LAUNCH, EventType.SOLAR)
//        DataManager.getEvents(queryParams) { success, events ->
//            if (success) {
//                Log.d("GettingStartedActivity", "Events: $events")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to get events")
//            }
//        }

//        DataManager.getUserEvents() { success, events ->
//            if (success) {
//                Log.d("GettingStartedActivity", "User Events: $events")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to get user events")
//            }
//        }

        // works
        // Get all comments of a specific event
//        DataManager.getEventComments(event_id) { success, comments ->
//            if (success) {
//                Log.d("GettingStartedActivity", "Comments: $comments")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to get comments")
//            }
//        }

        // works
        // Gets all news articles
//        DataManager.getNews() { success, news ->
//            if (success) {
//                Log.d("GettingStartedActivity", "News: $news")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to get news")
//            }
//        }

        // works
        // Get all comments of a specific news article
//        DataManager.getNewsComments(article_id) { success, comments ->
//            if (success) {
//                Log.d("GettingStartedActivity", "Comments: $comments")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to get comments")
//            }
//        }

        // -------------------- USER INTERACTIONS API MICROSERVICE --------------------

        // works
        // Adds a like/dislike/unlike/undislike to a specific event
//        UserInteractionsManager.putEventLike(LikesDislikesType.LIKE, event_id) { success, response ->
//            if (success) {
//                Log.d("GettingStartedActivity", "Response: $response")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to like event")
//            }
//        }

        // works
        // Adds a comment to a specific event
//        UserInteractionsManager.putEventComment(event_id, "yah yah yeet comment") { success, response ->
//            if (success) {
//                Log.d("GettingStartedActivity", "Response: $response")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to comment on event")
//            }
//        }

        // works
        // Adds a like/dislike/unlike/undislike to a specific comment of a specific event
//        UserInteractionsManager.putEventCommentLike(LikesDislikesType.LIKE, event_id, comment_id) { success, response ->
//            if (success) {
//                Log.d("GettingStartedActivity", "Response: $response")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to like comment")
//            }
//        }

        // works
        // Adds a like/dislike/unlike/undislike to a specific news article
//        UserInteractionsManager.putNewsLike(LikesDislikesType.LIKE, article_id) { success, response ->
//            if (success) {
//                Log.d("GettingStartedActivity", "Response: $response")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to like news")
//            }
//        }

        // works
        // Adds a comment to a specific news article
//        UserInteractionsManager.putNewsComment(article_id, "joel should shave his head") { success, response ->
//            if (success) {
//                Log.d("GettingStartedActivity", "Response: $response")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to comment on news")
//            }
//        }

        // works
        // Adds a like/dislike/unlike/undislike to a specific comment of a specific news article
//        UserInteractionsManager.putNewsCommentLike(LikesDislikesType.UNDISLIKE, article_id, comment_id) { success, response ->
//            if (success) {
//                Log.d("GettingStartedActivity", "Response: $response")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to like comment")
//            }
//        }

        // works
        // Gets all event types that can be used as a filter (EventType.NEO, EventType.DONKI, EventType.LAUNCH, EventType.SOLAR)
        // not sure I really like the naming for this but we're not expecting to really use this endpoint, mainly here for completion
//        UserInteractionsManager.getSubscriptions() { success, subscriptions ->
//            if (success) {
//                Log.d("GettingStartedActivity", "Subscriptions: $subscriptions")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to get subscriptions")
//            }
//        }

        // works
        // Gets all news topic types defined in TopicsTypes (planets, eclipses, constellations, northernlights, comets, asteroids, blackholes)
        // again, we're not expecting to really use this endpoint, mainly here for completion
//        UserInteractionsManager.getTopics() { success, topics ->
//            if (success) {
//                Log.d("GettingStartedActivity", "Get Topics: $topics")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to get topics")
//            }
//        }

        // works
//        UserInteractionsManager.putUserSubscriptions(event_id) { success, subscriptions ->
//            if (success) {
//                Log.d("GettingStartedActivity", "Updated Subscriptions: $subscriptions")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to put user subscriptions")
//            }
//        }

        // works
        // Gets all the event types that the user is subscribed to (EventType.NEO, EventType.DONKI, EventType.LAUNCH, EventType.SOLAR)
//        UserInteractionsManager.getUserSubscription() { success, response ->
//            if (success) {
//                Log.d("GettingStartedActivity", "User Subscriptions: $response")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to subscribe")
//            }
//        }

//        UserInteractionsManager.deleteUserSubscription(event_id) { success, subscriptions ->
//            if (success) {
//                Log.d("GettingStartedActivity", "Deleted Subscriptions: $subscriptions")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to delete user subscriptions")
//            }
//        }

        // works
        // Updates all the topics that the user is interested in (TopicsType.PLANETS, TopicsType.ECLIPSES, TopicsType.CONSTELLATIONS, TopicsType.NORTHERNLIGHTS, TopicsType.COMETS, TopicsType.ASTEROIDS, TopicsType.BLACKHOLES)
//        val new_topics: List<TopicsType> = listOf(TopicsType.PLANETS, TopicsType.ECLIPSES)
//        UserInteractionsManager.putUserTopics(new_topics) { success, response ->
//            if (success) {
//                Log.d("GettingStartedActivity", "Response: $response")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to update user topics")
//            }
//        }

        // works
        // Gets all the topics that the user is interested in (TopicsType.PLANETS, TopicsType.ECLIPSES, TopicsType.CONSTELLATIONS, TopicsType.NORTHERNLIGHTS, TopicsType.COMETS, TopicsType.ASTEROIDS, TopicsType.BLACKHOLES)
//        UserInteractionsManager.getUserTopics() { success, response ->
//            if (success) {
//                Log.d("GettingStartedActivity", "User Topics: $response")
//            } else {
//                Log.e("GettingStartedActivity", "Error: Failed to get user topics")
//            }
//        }
    }
}