package com.ece452.spacexplorer.networking.api.auth

import com.ece452.spacexplorer.networking.models.auth.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.GET
import retrofit2.http.POST

// Define the API endpoints for the auth base URL
interface AuthApiService {
    @GET("/") // dummy api call
    fun getRoot(): Call<GetRootReturn>

    @POST("/user/register_account")
    fun postRegisterAccount(@Body request: RegisterAccountRequest): Call<RegisterAccountReturn>

    @POST("/user/login")
    fun postLogin(@Body request: LoginRequest): Call<LoginReturn>

    @POST("/user/logout")
    fun postLogout(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String
    ): Call<LogoutReturn>

    @GET("/user/profile")
    fun getProfile(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String
    ): Call<ProfileReturn>

    @POST("/user/update_account")
    fun postUpdateAccount(
        @Header("Auth-Username") username: String,
        @Header("Auth-SessionID") sessionId: String,
        @Body request: UserProfile
    ): Call<UpdateAccountReturn>
}