package com.example.mainchameleon

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mainchameleon.databinding.ActivityLoginScreenBinding
import com.example.mainchameleon.ui.loginScreen.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginScreenBinding
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance() // Direct initialization
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up button listeners
        binding.loginButton.setOnClickListener {
            login() // Handle login
        }

        binding.registerButton.setOnClickListener {
            // Start RegisterActivity when the register button is clicked
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()

        val emailErrorResId = loginViewModel.validateEmail(email)
        val passwordErrorResId = loginViewModel.validatePassword(password)

        // Display errors if validation fails
        if (emailErrorResId != null) {
            binding.emailInput.error = getString(emailErrorResId)  // Use getString() to resolve resource ID
            return
        }
        if (passwordErrorResId != null) {
            binding.passwordInput.error = getString(passwordErrorResId)  // Use getString() to resolve resource ID
            return
        }

        // Proceed with Firebase Authentication
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Handle FirebaseAuth exceptions
                    val exception = task.exception
                    val errorMessage = when ((exception as? FirebaseAuthException)?.errorCode) {
                        "ERROR_USER_NOT_FOUND" -> getString(R.string.error_user_not_found)
                        "ERROR_WRONG_PASSWORD" -> getString(R.string.error_wrong_password)
                        else -> getString(R.string.error_authentication_failed)
                    }

                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Login error", exception)
                }
            }
    }

    // Auth state listener to handle auto-login
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        // Add auth state listener when the activity starts
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        // Remove auth state listener when the activity stops
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}