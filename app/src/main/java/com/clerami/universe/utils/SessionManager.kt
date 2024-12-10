package com.clerami.universe.utils

import android.content.Context
import android.content.SharedPreferences
import com.clerami.universe.data.remote.response.LoginResponse
import com.google.gson.Gson

class SessionManager(context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


    companion object {
        const val PREFS_NAME = "user_session"
        const val KEY_USER_ID = "user_id"
        private const val KEY_LOGIN_RESULT = "login_response"
        const val KEY_USER_TOKEN = "user_token"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_USER_NAME = "user_name"
    }
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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

    fun saveUserUniversity(university: String) {
        prefs.edit().putString("userUniversity", university).apply()
    }

    fun getUserUniversity(): String? {
        return prefs.getString("userUniversity", null)
    }

    fun saveUserName(username: String) {
        prefs.edit().putString(KEY_USER_NAME, username).apply()
    }

    fun saveProfileImageUrl(imageUrl: String) {
        val editor = prefs.edit()
        editor.putString("profile_image_url", imageUrl)
        editor.apply()
    }

    fun getProfileImageUrl(): String? {
        return prefs.getString("profile_image_url", null)
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}

