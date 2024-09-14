package com.ece452.spacexplorer.networking.models.auth

data class LoginRequest(
    val username: String,
    val password: String
)
