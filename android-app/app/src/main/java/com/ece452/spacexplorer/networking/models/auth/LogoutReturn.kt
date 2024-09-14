package com.ece452.spacexplorer.networking.models.auth

data class LogoutReturn(
    val message: String,
    val detail: LogoutReturnDetail
)

data class LogoutReturnDetail(
    val username: String,
    val session_id: String
)
