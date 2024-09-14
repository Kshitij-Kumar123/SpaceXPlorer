package com.ece452.spacexplorer.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

// To manage the session_id saved in local storage
object UsernameManager {

    private const val PREFS_NAME = "UserPrefs"
    private const val KEY_USERNAME = "username"

    fun setUsername(context: Context, username: String?) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USERNAME, username)
        editor.apply()
        Log.d("UsernameManager", "Username saved: $username")
    }

    fun getUsername(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    fun clearUsername(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(KEY_USERNAME)
        editor.apply()
        Log.d("UsernameManager", "Username cleared")
    }
}
