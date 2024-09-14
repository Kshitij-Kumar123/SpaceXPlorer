package com.ece452.spacexplorer.networking.api.userinteractions

import com.ece452.spacexplorer.networking.models.EventCommentResponse
import com.ece452.spacexplorer.networking.models.NewsCommentResponse
import com.ece452.spacexplorer.networking.models.userinteractions.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Define the API endpoints for the User Interactions base URL
interface UserInteractionsApiService {
    @PUT("/events/likes/{action}")
    fun putEventLike(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String,
        @Path("action") action: LikesDislikesType,
        @Query("event_id") event_id: String
    ): Call<LikesDislikesResponse>

    @PUT("/events/comments")
    fun putEventComment(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String,
        @Query("event_id") event_id: String,
        @Query("comment") comment: String,
    ): Call<EventCommentResponse>

    @PUT("/events/comments/likes/{action}")
    fun putEventCommentLike(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String,
        @Path("action") action: LikesDislikesType,
        @Query("event_id") event_id: String,
        @Query("comment_id") comment_id: String,
    ): Call<LikesDislikesResponse>

    @PUT("/news/likes/{action}")
    fun putNewsLike(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String,
        @Path("action") action: LikesDislikesType,
        @Query("article_id") article_id: String,
    ): Call<LikesDislikesResponse>

    @PUT("/news/comments")
    fun putNewsComment(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String,
        @Query("article_id") article_id: String,
        @Query("comment") comment: String,
    ): Call<NewsCommentResponse>

    @PUT("/news/comments/likes/{action}")
    fun putNewsCommentLike(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String,
        @Path("action") action: LikesDislikesType,
        @Query("article_id") article_id: String,
        @Query("comment_id") comment_id: String,
    ): Call<LikesDislikesResponse>

    @GET("/subscriptions")
    fun getSubscriptions(): Call<List<String>>

    @GET("/topics")
    fun getTopics(): Call<List<String>>

    @PUT("/users/subscriptions")
    fun putUserSubscriptions(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String,
        @Query("event_id") event_id: String,
    ): Call<List<String>>

    @GET("/users/subscriptions")
    fun getUserSubscription(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String,
    ): Call<List<String>>

    @HTTP(method = "DELETE", path = "/users/subscriptions", hasBody = true)
    fun deleteUserSubscription(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String,
        @Query("event_id") event_id: String,
    ): Call<List<String>>

    @PUT("/users/topics")
    fun putUserTopics(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String,
        @Query("topics") topics: List<String>,
    ): Call<List<String>>

    @GET("/users/topics")
    fun getUserTopics(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String,
    ): Call<List<String>>
}