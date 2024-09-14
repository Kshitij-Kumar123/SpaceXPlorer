package com.ece452.spacexplorer.networking.models.auth

data class LoginReturn(
    val message: String,
    val detail: LoginReturnDetail
)

data class LoginReturnDetail(
    val username: String,
    val session_id: String
)