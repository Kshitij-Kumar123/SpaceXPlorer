package com.ece452.spacexplorer.networking.models.auth

data class RegisterAccountReturn(
    val message: String,
    val detail: RegisterAccountReturnDetail
)

data class RegisterAccountReturnDetail(
    val username: String,
    val email: String
)