package com.ece452.spacexplorer.networking.models.data

import com.google.gson.*
import java.lang.reflect.Type

// Custom deserializer for EventResponse
class EventResponseDeserializer : JsonDeserializer<EventResponse> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): EventResponse {
        val jsonObject = json.asJsonObject
        val eventType = jsonObject.get("event_type").asString

        return when (eventType) {
            "neo" -> context.deserialize(json, Neo::class.java)
            "donki" -> context.deserialize(json, Donki::class.java)
            "launch" -> context.deserialize(json, Launch::class.java)
            "solar" -> context.deserialize(json, Solar::class.java)
            else -> throw JsonParseException("Unknown event type: $eventType")
        }
    }
}
