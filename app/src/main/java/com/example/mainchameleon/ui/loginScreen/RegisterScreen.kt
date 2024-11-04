// registerScreen.kt
package com.example.mainchameleon.ui.loginScreen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mainchameleon.MainActivity
import com.example.mainchameleon.databinding.RegisterScreenBinding
import com.example.mainchameleon.ui.userProfile.UserProfileUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: RegisterScreenBinding

    private var profileImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.registerButton.setOnClickListener {
            val firstName = binding.fnameEdit.text.toString().trim()
            val lastName = binding.lnameEdit.text.toString().trim()
            val username = binding.usernameEdit.text.toString().trim()
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
                    profileImageUri?.let { uri ->
                        UserProfileUtils.uploadProfilePicture(this, userId, uri) { profileImageUrl ->
                            saveUserToDatabase(userId, firstName, lastName, username, email, profileImageUrl)
                        }
                    } ?: saveUserToDatabase(userId, firstName, lastName, username, email, "")
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToDatabase(userId: String, firstName: String, lastName: String, username: String, email: String, profileImageUrl: String) {
        val userRef = database.getReference("Users").child(userId)
        val userMap = mapOf(
            "First Name" to firstName,
            "Last Name" to lastName,
            "Username" to username,
            "Email" to email,
            "profilePictureUrl" to profileImageUrl
        )

        userRef.setValue(userMap).addOnCompleteListener { dbTask ->
            if (dbTask.isSuccessful) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Database error: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}