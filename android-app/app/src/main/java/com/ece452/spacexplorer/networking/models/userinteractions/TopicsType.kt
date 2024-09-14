package com.ece452.spacexplorer.networking.models.userinteractions

enum class TopicsType(val value: String) {
    PLANETS("Planets"),
    ECLIPSES("Eclipses"),
    ASTRONAUTS("Astronauts"),
    NASA("NASA"),
    ASTEROIDS("Asteroids"),
    SOLARSYSTEM("Solar System");

    // API endpoint expects lowercase
    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): TopicsType? {
            return entries.find { it.value == value }
        }
    }
}