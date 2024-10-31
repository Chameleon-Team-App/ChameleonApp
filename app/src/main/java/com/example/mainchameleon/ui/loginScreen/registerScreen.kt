package com.example.mainchameleon.ui.loginScreen

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mainchameleon.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.mainchameleon.databinding.RegisterScreenBinding

class registerScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: RegisterScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.registerButton.setOnClickListener {
            val firstName = binding.fnameEdit.text.toString().trim()
            val lastName = binding.lnameEdit.text.toString().trim()
            val username = binding.usernameEdit.text.toString().trim() // Add username field
            val email = binding.emailEdit.text.toString().trim()
            val password = binding.passwordEdit.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(firstName, lastName, username, email, password)
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun registerUser(firstName: String, lastName: String, username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    val userRef = database.getReference("Users").child(userId)

                    // Store user info, including username
                    val userMap = mapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "username" to username,
                        "email" to email
                    )

                    userRef.setValue(userMap).addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Database error: " + dbTask.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: " + task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }
}
