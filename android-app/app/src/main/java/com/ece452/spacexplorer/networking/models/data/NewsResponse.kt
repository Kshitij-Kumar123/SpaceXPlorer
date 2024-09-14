package com.ece452.spacexplorer.networking.models.data

data class NewsResponse(
    val article_id: String,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val like_status: String = "",
    val author: String,
    val title: String,
    val description: String,
    val url: String,
    val url_to_image: String,
    val published_at: String,
    val content: String,
    val comment_count: Int = 0
)
