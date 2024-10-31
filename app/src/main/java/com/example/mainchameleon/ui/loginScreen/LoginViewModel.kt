package com.example.mainchameleon.ui.loginScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.core.util.PatternsCompat
import com.example.mainchameleon.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class LoginViewModel : ViewModel() {

    // Firebase instance
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // LiveData for validation errors
    private val _emailError = MutableLiveData<Int?>().apply { value = null }
    val emailError: LiveData<Int?> = _emailError

    private val _passwordError = MutableLiveData<Int?>().apply { value = null }
    val passwordError: LiveData<Int?> = _passwordError

    // LiveData for login result
    private val _loginResult = MutableLiveData<Result<String?>?>()
    val loginResult: LiveData<Result<String?>?> = _loginResult

    // Validate email and update _emailError LiveData
    fun validateEmail(email: String) {
        _emailError.value = when {
            email.isEmpty() -> R.string.error_email_required
            !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches() -> R.string.error_email_invalid
            else -> null
        }
    }

    // Validate password and update _passwordError LiveData
    fun validatePassword(password: String) {
        _passwordError.value = when {
            password.isEmpty() -> R.string.error_password_required
            else -> null
        }
    }

    // Firebase login function
    fun login(email: String, password: String) {
        // Clear previous login result
        _loginResult.value = null

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginResult.value = Result.success("Login successful")
                } else {
                    val errorMessageResId = when ((task.exception as? FirebaseAuthException)?.errorCode) {
                        "ERROR_USER_NOT_FOUND" -> R.string.error_User_not_found
                        "ERROR_WRONG_PASSWORD" -> R.string.error_wrong_password
                        else -> R.string.error_authentication_failed
                    }
                    _loginResult.value = Result.failure(Exception(getErrorMessage(errorMessageResId)))
                }
            }
    }

    // Helper function to retrieve error message from resources
    private fun getErrorMessage(errorResId: Int): String {
        return firebaseAuth.app.applicationContext.getString(errorResId)
    }
}