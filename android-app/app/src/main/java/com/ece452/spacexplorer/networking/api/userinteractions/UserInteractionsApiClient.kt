package com.ece452.spacexplorer.networking.api.userinteractions

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// DOCUMENTATION: https://square.github.io/retrofit/2.x/retrofit/
// represents a single static instance, and can never have any more or any less than this one instance
object UserInteractionsApiClient {
    private const val BASE_URL = "http://3.142.248.212:8003" // User Interactions API base URL

    // GsonConverterFactory class example: https://square.github.io/retrofit/
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Create an implementation of the API endpoints defined by the apiService interface
    fun <T> createService(apiService: Class<T>): T { // Generic type T
        return retrofit.create(apiService)
    }
}