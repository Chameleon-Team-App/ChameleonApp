package com.example.mainchameleon.ui.loginScreen

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mainchameleon.MainActivity
import com.example.mainchameleon.databinding.RegisterScreenBinding

class RegisterScreen : AppCompatActivity() {

    private lateinit var binding: RegisterScreenBinding
    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observing error messages
        registerViewModel.firstNameError.observe(this) { errorResId ->
            binding.fnameEdit.error = errorResId?.let { getString(it) }
        }

        registerViewModel.emailError.observe(this) { errorResId ->
            binding.emailEdit.error = errorResId?.let { getString(it) }
        }

        binding.registerButton.setOnClickListener {
            val firstName = binding.fnameEdit.text.toString().trim()
            val email = binding.emailEdit.text.toString().trim()

            registerViewModel.validateFirstName(firstName)
            registerViewModel.validateEmail(email)

            // Proceed with registration if no errors
            if (registerViewModel.firstNameError.value == null &&
                registerViewModel.emailError.value == null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}