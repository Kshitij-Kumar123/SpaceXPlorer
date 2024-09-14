package com.ece452.spacexplorer.networking.models.auth

data class UpdateAccountReturn(
    val username: String,
    val email: String,
    val password: String,
    val phone_number: String,
    val subscriptions: List<String>
)
