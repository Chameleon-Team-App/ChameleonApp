package com.example.mainchameleon

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mainchameleon.databinding.LoginScreenBinding
import com.example.mainchameleon.ui.loginScreen.LoginViewModel
import com.example.mainchameleon.ui.loginScreen.registerScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginScreenBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.loginButton.setOnClickListener {
            val input = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (input.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                if (Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                    // If input is an email, log in with email
                    loginWithEmail(input, password)
                } else {
                    // If input is not an email, assume it's a Username
                    loginWithUsername(input, password)
                }
            }
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, registerScreen::class.java))
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login failed: " + task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loginWithUsername(Username: String, password: String) {
        // Query the database to find the email associated with the Username
        val UserRef = database.getReference("Users")
        UserRef.orderByChild("Username").equalTo(Username).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    for (child in dataSnapshot.children) {
                        val email = child.child("email").value.toString()
                        loginWithEmail(email, password)
                    }
                } else {
                    Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: " + it.message, Toast.LENGTH_SHORT).show()
            }
    }
}
