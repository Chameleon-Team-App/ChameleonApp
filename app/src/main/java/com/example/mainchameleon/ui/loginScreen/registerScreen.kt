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
            val Username = binding.usernameEdit.text.toString().trim() // Add Username field
            val email = binding.emailEdit.text.toString().trim()
            val password = binding.passwordEdit.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || Username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(firstName, lastName, Username, email, password)
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun registerUser(firstName: String, lastName: String, Username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val UserId = auth.currentUser?.uid ?: ""
                    val UserRef = database.getReference("Users").child(UserId)

                    // Store User info, including Username
                    val UserMap = mapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "Username" to Username,
                        "email" to email
                    )

                    UserRef.setValue(UserMap).addOnCompleteListener { dbTask ->
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
