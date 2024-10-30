package com.example.mainchameleon

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mainchameleon.databinding.LoginScreenBinding
import com.example.mainchameleon.ui.loginScreen.LoginViewModel
import com.example.mainchameleon.ui.loginScreen.RegisterScreen

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginScreenBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

        binding.loginButton.setOnClickListener {
            val email = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            loginViewModel.validateEmail(email)
            loginViewModel.validatePassword(password)

            if (loginViewModel.emailError.value == null && loginViewModel.passwordError.value == null) {
                loginViewModel.login(email, password)
            }
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterScreen::class.java))
        }
    }

    private fun setupObservers() {
        loginViewModel.emailError.observe(this) { errorResId ->
            binding.usernameInput.error = errorResId?.let { getString(it) }
        }

        loginViewModel.passwordError.observe(this) { errorResId ->
            binding.passwordInput.error = errorResId?.let { getString(it) }
        }

        loginViewModel.loginResult.observe(this) { result ->
            result?.onSuccess {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }?.onFailure { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}