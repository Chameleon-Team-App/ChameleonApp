package com.example.mainchameleon.ui.loginScreen

data class UserProfile(
    val firstName: String,
    val middleName: String? = null, // Optional middle name
    val lastName: String,
    val email: String
)