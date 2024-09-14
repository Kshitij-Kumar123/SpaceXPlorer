package com.ece452.spacexplorer.utils

import java.security.MessageDigest

object Hasher {
    fun toSHA256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}