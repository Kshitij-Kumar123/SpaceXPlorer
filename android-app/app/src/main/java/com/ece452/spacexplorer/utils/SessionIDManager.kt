package com.ece452.spacexplorer.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

// To manage the session_id saved in local storage
object SessionIDManager {

    private const val PREFS_NAME = "SessionPrefs"
    private const val KEY_SESSION_ID = "session_id"

    fun setSessionID(context: Context, sessionID: String?) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_SESSION_ID, sessionID)
        editor.apply()
        Log.d("SessionIDManager", "Session ID saved: $sessionID")
    }

    fun getSessionID(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_SESSION_ID, null)
    }

    fun clearSessionID(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(KEY_SESSION_ID)
        editor.apply()
        Log.d("SessionIDManager", "Session ID cleared")
    }
}
