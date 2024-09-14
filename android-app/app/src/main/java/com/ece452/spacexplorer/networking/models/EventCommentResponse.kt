package com.ece452.spacexplorer.networking.models

data class EventCommentResponse(
    val comment_id: String,
    val event_id: String,
    val username: String,
    val timestamp: String,
    val comment: String,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val like_status: String = "",
)
