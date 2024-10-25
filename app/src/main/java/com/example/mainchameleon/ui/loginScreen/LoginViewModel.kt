package com.example.mainchameleon.ui.loginScreen

import androidx.lifecycle.ViewModel
import androidx.core.util.PatternsCompat
import com.example.mainchameleon.R

class LoginViewModel : ViewModel() {

    fun validateEmail(email: String): Int? {
        return when {
            email.isEmpty() -> R.string.error_email_required
            !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches() -> R.string.error_email_invalid
            else -> null // Return null if no error
        }
    }

    fun validatePassword(password: String): Int? {
        return when {
            password.isEmpty() -> R.string.error_password_required
            else -> null // Return null if no error
        }
    }
}