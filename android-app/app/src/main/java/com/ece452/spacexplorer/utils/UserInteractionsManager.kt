package com.ece452.spacexplorer.utils

import android.content.Context
import android.util.Log
import com.ece452.spacexplorer.networking.api.userinteractions.*
import com.ece452.spacexplorer.networking.models.EventCommentResponse
import com.ece452.spacexplorer.networking.models.NewsCommentResponse
import com.ece452.spacexplorer.networking.models.userinteractions.*

// IMPORTS FOR API CALLS
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// To abstract away the API calls and provide a simpler interface for the rest of the app to use
object UserInteractionsManager {

    // Create an implementation of the API endpoints defined by the apiService interface
    private var userInteractionsApiService: UserInteractionsApiService = UserInteractionsApiClient.createService(UserInteractionsApiService::class.java)

    private lateinit var appContext: Context
    private lateinit var username: String
    private lateinit var session_id: String

    fun init(context: Context) {
        appContext = context.applicationContext
        username = UsernameManager.getUsername(appContext).toString()
        session_id = SessionIDManager.getSessionID(appContext).toString()
    }

    // for Instrumented Tests only
    fun update_user_session(username: String, session_id: String) {
        UserInteractionsManager.username=username
        UserInteractionsManager.session_id = session_id
    }


    fun putEventLike(action: LikesDislikesType, event_id: String, putEventLikeCallback: (Boolean, LikesDislikesResponse?) -> Unit) {
        userInteractionsApiService.putEventLike(username, session_id, action, event_id).enqueue(object : Callback<LikesDislikesResponse> {
            override fun onResponse(call: Call<LikesDislikesResponse>, response: Response<LikesDislikesResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("UserInteractionsManager", "Data: $data")

                    putEventLikeCallback(true, data)
                } else {
                    Log.e("UserInteractionsManager", "Error: ${response.errorBody()?.string()}")
                    putEventLikeCallback(false, null)
                }
            }

            override fun onFailure(call: Call<LikesDislikesResponse>, t: Throwable) {
                Log.e("UserInteractionsManager", "Failure: ${t.message}")
                putEventLikeCallback(false, null)
            }
        })
    }

    fun putEventComment(event_id: String, comment: String, putEventCommentCallback: (Boolean, EventCommentResponse?) -> Unit) {
        userInteractionsApiService.putEventComment(username, session_id,event_id, comment).enqueue(object : Callback<EventCommentResponse> {
            override fun onResponse(call: Call<EventCommentResponse>, response: Response<EventCommentResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("UserInteractionsManager", "Data: $data")

                    putEventCommentCallback(true, data)
                } else {
                    Log.e("UserInteractionsManager", "Error: ${response.errorBody()?.string()}")
                    putEventCommentCallback(false, null)
                }
            }

            override fun onFailure(call: Call<EventCommentResponse>, t: Throwable) {
                Log.e("UserInteractionsManager", "Failure: ${t.message}")
                putEventCommentCallback(false, null)
            }
        })
    }

    fun putEventCommentLike(action: LikesDislikesType, event_id: String, comment_id: String, putEventCommentLikeCallback: (Boolean, LikesDislikesResponse?) -> Unit) {
        userInteractionsApiService.putEventCommentLike(username, session_id,action, event_id, comment_id).enqueue(object : Callback<LikesDislikesResponse> {
            override fun onResponse(call: Call<LikesDislikesResponse>, response: Response<LikesDislikesResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("UserInteractionsManager", "Data: $data")

                    putEventCommentLikeCallback(true, data)
                } else {
                    Log.e("UserInteractionsManager", "Error: ${response.errorBody()?.string()}")
                    putEventCommentLikeCallback(false, null)
                }
            }

            override fun onFailure(call: Call<LikesDislikesResponse>, t: Throwable) {
                Log.e("UserInteractionsManager", "Failure: ${t.message}")
                putEventCommentLikeCallback(false, null)
            }
        })
    }

    fun putNewsLike(action: LikesDislikesType, article_id: String, putNewsLikeCallback: (Boolean, LikesDislikesResponse?) -> Unit) {
        userInteractionsApiService.putNewsLike(username, session_id,action, article_id).enqueue(object : Callback<LikesDislikesResponse> {
            override fun onResponse(call: Call<LikesDislikesResponse>, response: Response<LikesDislikesResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("UserInteractionsManager", "Data: $data")

                    putNewsLikeCallback(true, data)
                } else {
                    Log.e("UserInteractionsManager", "Error: ${response.errorBody()?.string()}")
                    putNewsLikeCallback(false, null)
                }
            }

            override fun onFailure(call: Call<LikesDislikesResponse>, t: Throwable) {
                Log.e("UserInteractionsManager", "Failure: ${t.message}")
                putNewsLikeCallback(false, null)
            }
        })
    }

    fun putNewsComment(article_id: String, comment: String, putNewsCommentCallback: (Boolean, NewsCommentResponse?) -> Unit) {
        userInteractionsApiService.putNewsComment(username, session_id,article_id, comment).enqueue(object : Callback<NewsCommentResponse> {
            override fun onResponse(call: Call<NewsCommentResponse>, response: Response<NewsCommentResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("UserInteractionsManager", "Data: $data")

                    putNewsCommentCallback(true, data)
                } else {
                    Log.e("UserInteractionsManager", "Error: ${response.errorBody()?.string()}")
                    putNewsCommentCallback(false, null)
                }
            }

            override fun onFailure(call: Call<NewsCommentResponse>, t: Throwable) {
                Log.e("UserInteractionsManager", "Failure: ${t.message}")
                putNewsCommentCallback(false, null)
            }
        })
    }

    fun putNewsCommentLike(action: LikesDislikesType, article_id: String, comment_id: String, putNewsCommentLikeCallback: (Boolean, LikesDislikesResponse?) -> Unit) {
        userInteractionsApiService.putNewsCommentLike(username, session_id,action, article_id, comment_id).enqueue(object : Callback<LikesDislikesResponse> {
            override fun onResponse(call: Call<LikesDislikesResponse>, response: Response<LikesDislikesResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("UserInteractionsManager", "Data: $data")

                    putNewsCommentLikeCallback(true, data)
                } else {
                    Log.e("UserInteractionsManager", "Error: ${response.errorBody()?.string()}")
                    putNewsCommentLikeCallback(false, null)
                }
            }

            override fun onFailure(call: Call<LikesDislikesResponse>, t: Throwable) {
                Log.e("UserInteractionsManager", "Failure: ${t.message}")
                putNewsCommentLikeCallback(false, null)
            }
        })
    }

    fun getSubscriptions(getSubscriptionsCallback: (Boolean, List<String>?) -> Unit) {
        userInteractionsApiService.getSubscriptions().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("UserInteractionsManager", "Data: $data")

                    getSubscriptionsCallback(true, data)
                } else {
                    Log.e("UserInteractionsManager", "Error: ${response.errorBody()?.string()}")
                    getSubscriptionsCallback(false, null)
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e("UserInteractionsManager", "Failure: ${t.message}")
                getSubscriptionsCallback(false, null)
            }
        })
    }

    fun getTopics(getTopicsCallback: (Boolean, List<String>?) -> Unit) {
        userInteractionsApiService.getTopics().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("UserInteractionsManager", "Data: $data")

                    getTopicsCallback(true, data)
                } else {
                    Log.e("UserInteractionsManager", "Error: ${response.errorBody()?.string()}")
                    getTopicsCallback(false, null)
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e("UserInteractionsManager", "Failure: ${t.message}")
                getTopicsCallback(false, null)
            }
        })
    }

    // topics is list of strings for now, TODO: change to proper data class later depending on what we decide later?
    fun putUserSubscriptions(event_id: String, getUserSubscriptionsCallback: (Boolean, List<String>?) -> Unit) {
        userInteractionsApiService.putUserSubscriptions(username, session_id,event_id).enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("UserInteractionsManager", "Data: $data")

                    getUserSubscriptionsCallback(true, data)
                } else {
                    Log.e("UserInteractionsManager", "Error: ${response.errorBody()?.string()}")
                    getUserSubscriptionsCallback(false, null)
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e("UserInteractionsManager", "Failure: ${t.message}")
                getUserSubscriptionsCallback(false, null)
            }
        })
    }

    fun getUserSubscription(putUserSubscriptionCallback: (Boolean, List<String>?) -> Unit) {
        userInteractionsApiService.getUserSubscription(username, session_id).enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("UserInteractionsManager", "Data: $data")

                    putUserSubscriptionCallback(true, data)
                } else {
                    Log.e("UserInteractionsManager", "Error: ${response.errorBody()?.string()}")
                    putUserSubscriptionCallback(false, null)
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e("UserInteractionsManager", "Failure: ${t.message}")
                putUserSubscriptionCallback(false, null)
            }
        })
    }

    fun deleteUserSubscription(event_id: String, deleteUserSubscriptionCallback: (Boolean, List<String>?) -> Unit) {
        userInteractionsApiService.deleteUserSubscription(username, session_id,event_id).enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("UserInteractionsManager", "Data: $data")

                    deleteUserSubscriptionCallback(true, data)
                } else {
                    Log.e("UserInteractionsManager", "Error: ${response.errorBody()?.string()}")
                    deleteUserSubscriptionCallback(false, null)
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e("UserInteractionsManager", "Failure: ${t.message}")
                deleteUserSubscriptionCallback(false, null)
            }
        })
    }

    fun putUserTopics(topics: List<TopicsType>, putUserTopicsCallback: (Boolean, List<String>?) -> Unit) {
        userInteractionsApiService.putUserTopics(username, session_id,topics.map { it. value }).enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("UserInteractionsManager", "Data: $data")

                    putUserTopicsCallback(true, data)
                } else {
                    Log.e("UserInteractionsManager", "Error: ${username} ${session_id}")
                    putUserTopicsCallback(false, null)
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e("UserInteractionsManager", "Failure: ${t.message}")
                putUserTopicsCallback(false, null)
            }
        })
    }

    fun getUserTopics(getUserTopicsCallback: (Boolean, List<String>?) -> Unit) {
        userInteractionsApiService.getUserTopics(username, session_id).enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("UserInteractionsManager", "Data: $data")

                    getUserTopicsCallback(true, data)
                } else {
                    Log.e("UserInteractionsManager", "Error: ${response.errorBody()?.string()}")
                    getUserTopicsCallback(false, null)
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e("UserInteractionsManager", "Failure: ${t.message}")
                getUserTopicsCallback(false, null)
            }
        })
    }
}