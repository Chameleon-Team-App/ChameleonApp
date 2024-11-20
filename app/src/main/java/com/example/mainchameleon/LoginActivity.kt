package com.example.mainchameleon

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mainchameleon.databinding.LoginScreenBinding
import com.example.mainchameleon.utils.SessionManager
import com.example.mainchameleon.ui.loginScreen.RegisterScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
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
            startActivity(Intent(this, RegisterScreen::class.java))
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Save session details
                        sessionManager.saveUserSession(userId, email)
                    }

                    // Navigate to the dashboard
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loginWithUsername(username: String, password: String) {
        val userRef = database.getReference("Users")
        userRef.orderByChild("Username").equalTo(username).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val email = dataSnapshot.children.firstOrNull()?.child("email")?.value?.toString()
                    val userId = dataSnapshot.children.firstOrNull()?.key ?: ""
                    if (!email.isNullOrEmpty()) {
                        loginWithEmail(email, password)
                        val sessionManager = SessionManager(this)
                        sessionManager.saveUserSession(userId, username) // Save both userId and username
                    } else {
                        Toast.makeText(this, "Associated email not found for the username", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getUsernameFromDatabase(userId: String): String? {
        var username: String? = null
        val userRef = database.getReference("Users").child(userId)
        userRef.child("Username").get()
            .addOnSuccessListener { snapshot ->
                username = snapshot.getValue(String::class.java)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to retrieve username: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        return username
    }

}
