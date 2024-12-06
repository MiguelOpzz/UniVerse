package com.clerami.universe.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_NAME = "user_session"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_TOKEN = "user_token"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_USER_NAME = "user_name"
    }

    fun saveSession(token: String, email: String, username:String) {
        val editor = prefs.edit()
        editor.putString(KEY_USER_TOKEN, token)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_USER_NAME,username )
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun saveUserData(token:String ,username: String, email: String) {
        val editor = prefs.edit()
        editor.putString(KEY_USER_TOKEN, token)
        editor.putString(KEY_USER_NAME, username)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun getUserToken(): String? {
        return prefs.getString(KEY_USER_TOKEN, null)
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}

