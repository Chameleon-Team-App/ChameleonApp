package com.example.mainchameleon.ui.loginScreen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mainchameleon.MainActivity
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.RegisterScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class registerScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var binding: RegisterScreenBinding

    private var profileImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        // Listen for result from CameraFragment
        supportFragmentManager.setFragmentResultListener("photoResult", this) { _, bundle ->
            val photoUriString = bundle.getString("photoUrl")
            photoUriString?.let {
                profileImageUri = Uri.parse(it)
                binding.profileImagePreview.setImageURI(profileImageUri) // Update profile image preview
            }
        }

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

        binding.buttonOpenCamera.setOnClickListener { openCamera() }
        binding.buttonOpenGallery.setOnClickListener { openGallery() }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun openCamera() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("navigateToCamera", true)
        intent.putExtra("source", "register") // Specify source as "register"
        startActivity(intent)
    }



    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_GALLERY -> {
                    profileImageUri = data?.data
                    binding.profileImagePreview.setImageURI(profileImageUri)
                }
            }
        }
    }

    private fun registerUser(firstName: String, lastName: String, username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    uploadProfilePicture(userId) { profileImageUrl ->
                        val userRef = database.getReference("Users").child(userId)

                        val userMap = mapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "Username" to username,
                            "email" to email,
                            "profilePictureUrl" to profileImageUrl
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
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: " + task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadProfilePicture(userId: String, onComplete: (String) -> Unit) {
        profileImageUri?.let { uri ->
            val storageRef = storage.reference.child("Users/$userId/profilePictures/${uri.lastPathSegment}")
            storageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                        onComplete(downloadUri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload profile picture.", Toast.LENGTH_SHORT).show()
                }
        } ?: onComplete("")
    }

    companion object {
        private const val REQUEST_GALLERY = 2
    }
}
