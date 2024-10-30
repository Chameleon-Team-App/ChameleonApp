package com.example.mainchameleon.ui.loginScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.core.util.PatternsCompat
import com.example.mainchameleon.R

class RegisterViewModel : ViewModel() {

    private val _firstNameError = MutableLiveData<Int?>()
    val firstNameError: LiveData<Int?> = _firstNameError

    private val _emailError = MutableLiveData<Int?>()
    val emailError: LiveData<Int?> = _emailError

    fun validateFirstName(firstName: String) {
        _firstNameError.value = if (firstName.isEmpty()) R.string.error_first_name_required else null
    }

    fun validateEmail(email: String) {
        _emailError.value = when {
            email.isEmpty() -> R.string.error_email_required
            !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches() -> R.string.error_email_invalid
            else -> null
        }
    }
}