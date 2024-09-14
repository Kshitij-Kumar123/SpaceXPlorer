package com.ece452.spacexplorer.networking.models.userinteractions

enum class LikesDislikesType(val value: String) {
    LIKE("like"),
    DISLIKE("dislike"),
    UNLIKE("unlike"),
    UNDISLIKE("undislike");

    // API endpoint expects lowercase
    override fun toString(): String {
        return value
    }
}