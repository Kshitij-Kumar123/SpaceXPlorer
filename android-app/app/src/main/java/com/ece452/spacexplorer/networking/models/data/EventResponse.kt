package com.ece452.spacexplorer.networking.models.data

sealed class EventResponse(
    open val event_id: String = "",
    open val event_type: String = "",
    open val likes: Int = 0,
    open val dislikes: Int = 0,
    open val is_subscribed: Boolean = false,
    open val like_status: String = "",
    open val comment_count: Int = 0
)

// neo
data class Neo(
    val name: String,
    val is_hazardous: Boolean,
    val speed: Double,
    val diameter: Double,
    val approach_date: String,
    val miss_distance: String
) : EventResponse()

// donki
data class Donki(
    val message_id: String,
    val message_type: String,
    val message_url: String,
    val message_issue_time: String,
    val message_body: String
) : EventResponse()

// launch
data class Launch(
    val longitude: Double,
    val latitude: Double,
    val country: String,
    val pad_name: String,
    val name: String,
    val status_code: String,
    val status_desc: String,
    val last_updated: String,
    val service_provider: String,
    val rocket_name: String,
    val mission_name: String,
    val mission_type: String,
    val mission_info: String,
    val launch_image: String
) : EventResponse()

// solar
data class Solar(
    val longitude: Double,
    val latitude: Double,
    val timestamp: String,
    val central_duration: String,
    val path_width: Int,
    val eclipse_type: String
) : EventResponse()

fun propertiesToList(event: EventResponse): List<Pair<String, Any?>> {
    return when (event) {
        is Launch -> listOf(
            "event_id" to event.event_id,
            "event_type" to event.event_type,
            "likes" to event.likes,
            "dislikes" to event.dislikes,
            "is_subscribed" to event.is_subscribed,
            "like_status" to event.like_status,
            "longitude" to event.longitude,
            "latitude" to event.latitude,
            "country" to event.country,
            "pad_name" to event.pad_name,
            "name" to event.name,
            "status_code" to event.status_code,
            "status_desc" to event.status_desc,
            "last_updated" to event.last_updated,
            "service_provider" to event.service_provider,
            "rocket_name" to event.rocket_name,
            "mission_name" to event.mission_name,
            "mission_type" to event.mission_type,
            "mission_info" to event.mission_info,
            "launch_image" to event.launch_image
        )
        is Neo -> listOf(
            "event_type" to event.event_type,
            "likes" to event.likes,
            "is_subscribed" to event.is_subscribed,
            "like_status" to event.like_status,
            "name" to event.name,
            "is_hazardous" to event.is_hazardous,
            "speed" to event.speed,
            "dislikes" to event.dislikes,
            "event_id" to event.event_id,
            "diameter" to event.diameter,
            "approach_date" to event.approach_date,
            "miss_distance" to event.miss_distance
        )
        is Donki -> listOf(
            "event_id" to event.event_id,
            "event_type" to event.event_type,
            "likes" to event.likes,
            "dislikes" to event.dislikes,
            "is_subscribed" to event.is_subscribed,
            "like_status" to event.like_status,
            "message_id" to event.message_id,
            "message_type" to event.message_type,
            "message_url" to event.message_url,
            "message_issue_time" to event.message_issue_time,
            "message_body" to event.message_body
        )
        is Solar -> listOf(
            "event_id" to event.event_id,
            "event_type" to event.event_type,
            "likes" to event.likes,
            "dislikes" to event.dislikes,
            "is_subscribed" to event.is_subscribed,
            "like_status" to event.like_status,
            "longitude" to event.longitude,
            "latitude" to event.latitude,
            "timestamp" to event.timestamp,
            "central_duration" to event.central_duration,
            "path_width" to event.path_width,
            "eclipse_type" to event.eclipse_type
        )
    }
}
