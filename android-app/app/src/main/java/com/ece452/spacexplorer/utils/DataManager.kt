package com.ece452.spacexplorer.utils

import android.content.Context
import android.util.Log
import com.ece452.spacexplorer.networking.api.data.*
import com.ece452.spacexplorer.networking.models.EventCommentResponse
import com.ece452.spacexplorer.networking.models.NewsCommentResponse
import com.ece452.spacexplorer.networking.models.data.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object DataManager {

    private var dataApiService: DataApiService = DataApiClient.createService(DataApiService::class.java)

    private lateinit var appContext: Context
    private lateinit var username: String
    private lateinit var session_id: String
//    private lateinit var user_session: UserSession

    fun init(context: Context) {
        appContext = context.applicationContext
        username = UsernameManager.getUsername(appContext).toString()
        session_id = SessionIDManager.getSessionID(appContext).toString()
//        Log.d("DataManager", "User Session Initialization: $user_session")
    }

    // for Instrumented Tests only
    fun update_user_session(username: String, session_id: String) {
        DataManager.username = username
        DataManager.session_id = session_id
    }


    fun getEvents(eventTypes: List<EventType>?, getEventsCallback: (Boolean, ArrayList<EventResponse>?) -> Unit) {
        Log.d("DataManager", "Query Params Received: $eventTypes")
        if (eventTypes != null) {
            Log.d("DataManager", "Query Params Received: ${eventTypes.map { it.value }}")
        }

        if (eventTypes != null) {
            dataApiService.getEvents(username, session_id, eventTypes.map { it.value }).enqueue(object : Callback<List<EventResponse>> {
                override fun onResponse(call: Call<List<EventResponse>>, response: Response<List<EventResponse>>) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        Log.d("DataManager", "Data: $data")
                        Log.d("DataManager", "Data Length: ${data?.size ?: 0}")
                        Log.d("DataManager", "Data Type: ${data?.javaClass?.simpleName}")

                        // Convert to ArrayList and pass to the callback
                        getEventsCallback(true, data?.let { ArrayList(it) })
                    } else {
                        Log.e("DataManager", "Error: ${response.errorBody()?.string()}")
                        getEventsCallback(false, null)
                    }
                }

                override fun onFailure(call: Call<List<EventResponse>>, t: Throwable) {
                    Log.e("DataManager", "Failure: ${t.message}")
                    getEventsCallback(false, null)
                }
            })
        }
    }

    fun getUserEvents(getUserEventsCallback: (Boolean, ArrayList<EventResponse>?) -> Unit) {
        dataApiService.getUserEvents(username, session_id).enqueue(object : Callback<List<EventResponse>> {
            override fun onResponse(call: Call<List<EventResponse>>, response: Response<List<EventResponse>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("DataManager", "Data: $data")
                    Log.d("DataManager", "Data Length: ${data?.size ?: 0}")
                    Log.d("DataManager", "Data Type: ${data?.javaClass?.simpleName}")

                    getUserEventsCallback(true, data?.let { ArrayList(it) })
                } else {
                    Log.e("DataManager", "Error: ${response.errorBody()?.string()}")
                    getUserEventsCallback(false, null)
                }
            }

            override fun onFailure(call: Call<List<EventResponse>>, t: Throwable) {
                Log.e("DataManager", "Failure: ${t.message}")
                getUserEventsCallback(false, null)
            }
        })
    }

    fun getEventComments(event_id: String, getEventCommentsCallback: (Boolean, ArrayList<EventCommentResponse>?) -> Unit) {
        dataApiService.getEventComments(username, session_id, event_id).enqueue(object : Callback<List<EventCommentResponse>> {
            override fun onResponse(call: Call<List<EventCommentResponse>>, response: Response<List<EventCommentResponse>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("DataManager", "Data: $data")
                    Log.d("DataManager", "Data Length: ${data?.size ?: 0}")
                    Log.d("DataManager", "Data Type: ${data?.javaClass?.simpleName}")

                    getEventCommentsCallback(true, data?.let { ArrayList(it) })
                } else {
                    Log.e("DataManager", "Error: ${response.errorBody()?.string()}")
                    getEventCommentsCallback(false, null)
                }
            }

            override fun onFailure(call: Call<List<EventCommentResponse>>, t: Throwable) {
                Log.e("DataManager", "Failure: ${t.message}")
                getEventCommentsCallback(false, null)
            }
        })
    }

    fun getNews(getNewsCallback: (Boolean, ArrayList<NewsResponse>?) -> Unit) {
        dataApiService.getNews(username, session_id).enqueue(object : Callback<List<NewsResponse>> {
            override fun onResponse(call: Call<List<NewsResponse>>, response: Response<List<NewsResponse>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("DataManager", "Data: $data")
                    Log.d("DataManager", "Data Length: ${data?.size ?: 0}")
                    Log.d("DataManager", "Data Type: ${data?.javaClass?.simpleName}")

                    // Convert to ArrayList and pass to the callback
                    getNewsCallback(true, data?.let { ArrayList(it) })
                } else {
                    Log.e("DataManager", "Error: ${response.errorBody()?.string()}")
                    getNewsCallback(false, null)
                }
            }

            override fun onFailure(call: Call<List<NewsResponse>>, t: Throwable) {
                Log.e("DataManager", "Failure: ${t.message}")
                getNewsCallback(false, null)
            }
        })
    }

    fun getNewsComments(article_id: String, getNewsCommentsCallback: (Boolean, ArrayList<NewsCommentResponse>?) -> Unit) {
        dataApiService.getNewsComments(username, session_id,article_id).enqueue(object : Callback<List<NewsCommentResponse>> {
            override fun onResponse(call: Call<List<NewsCommentResponse>>, response: Response<List<NewsCommentResponse>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("DataManager", "Data: $data")
                    Log.d("DataManager", "Data Length: ${data?.size ?: 0}")
                    Log.d("DataManager", "Data Type: ${data?.javaClass?.simpleName}")

                    getNewsCommentsCallback(true, data?.let { ArrayList(it) })
                } else {
                    Log.e("DataManager", "Error: ${response.errorBody()?.string()}")
                    getNewsCommentsCallback(false, null)
                }
            }

            override fun onFailure(call: Call<List<NewsCommentResponse>>, t: Throwable) {
                Log.e("DataManager", "Failure: ${t.message}")
                getNewsCommentsCallback(false, null)
            }
        })
    }
}
