package com.ece452.spacexplorer.networking.api.data

import com.ece452.spacexplorer.networking.models.data.*
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DataApiClient {
    private const val BASE_URL = "http://3.142.248.212:8002" // Data API base URL

    // Custom Gson serializer for GET /events endpoint to deserialize EventResponse
    // Specifically registered for the EventResponse type, other types will use default Gson serialization and deserialization
    val gson = GsonBuilder()
        .registerTypeAdapter(EventResponse::class.java, EventResponseDeserializer())
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Create an implementation of the API endpoints defined by the apiService interface
    fun <T> createService(apiService: Class<T>): T { // Generic type T
        return retrofit.create(apiService)
    }
}