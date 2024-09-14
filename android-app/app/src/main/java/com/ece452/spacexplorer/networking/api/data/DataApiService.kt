package com.ece452.spacexplorer.networking.api.data

import com.ece452.spacexplorer.networking.models.EventCommentResponse
import com.ece452.spacexplorer.networking.models.NewsCommentResponse
import com.ece452.spacexplorer.networking.models.data.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface DataApiService {
    @GET("/events/all")
    fun getEvents(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String,
        @Query("filter") eventTypes: List<String>?,
    ): Call<List<EventResponse>>

    @GET("/events/user")
    fun getUserEvents(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String
    ): Call<List<EventResponse>>

    @GET("/events/comments")
    fun getEventComments(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String,
        @Query("event_id") event_id: String,
    ): Call<List<EventCommentResponse>>

    @GET("/news")
    fun getNews(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String
    ): Call<List<NewsResponse>>

    @GET("/news/comments")
    fun getNewsComments(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String,
        @Query("article_id") article_id: String,
    ): Call<List<NewsCommentResponse>>
}