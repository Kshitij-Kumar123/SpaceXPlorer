package com.ece452.spacexplorer.networking.models.auth

data class ProfileReturn(
    val message: String,
    val detail: ProfileReturnDetail
)

data class ProfileReturnDetail(
    val username: String,
    val email: String,
    val password: String,
    val phone_number: String
)
