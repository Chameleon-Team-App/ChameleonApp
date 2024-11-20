package com.example.mainchameleon.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    // Save user session with userId and username
    fun saveUserSession(userId: String, username: String) {
        val editor = prefs.edit()
        editor.putString("user_id", userId)
        editor.putString("username", username)
        editor.apply()
    }

    // Retrieve userId
    fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }

    // Retrieve username
    fun getUsername(): String? {
        return prefs.getString("username", null)
    }

    // Check if the user is logged in
    fun isLoggedIn(): Boolean {
        return getUserId() != null
    }

    // Clear session (logout)
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}