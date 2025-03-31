package com.example.mediaapplication

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper {

    private  val PREF_NAME = "myAppPrefs"
    private  val KEY_USER_ID = "user_id"

    fun saveUserIdToPrefs(context: Context, userId: String) {
        val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserIdFromPrefs(context: Context): String? {
        val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_USER_ID, null)  // Returns userId or null if not found
    }

    fun clearUserId(context: Context) {
        val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().remove(KEY_USER_ID).apply()
    }
}

