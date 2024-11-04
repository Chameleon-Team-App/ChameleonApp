package com.example.mainchameleon.ui.userProfile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mainchameleon.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileCustomizationFragment : Fragment() {

    private lateinit var buttonSaveProfile: Button
    private lateinit var profileImageView: ImageView
    private lateinit var bioEditText: EditText
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private var photoUri: Uri? = null
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var usernameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile_customization, container, false)

        profileImageView = rootView.findViewById(R.id.change_picture_button) // Initialize profileImageView
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        usernameTextView = rootView.findViewById(R.id.username_edit_text)
        firstNameEditText = rootView.findViewById(R.id.fname_edit_text)
        lastNameEditText = rootView.findViewById(R.id.lname_edit_text)
        bioEditText = rootView.findViewById(R.id.bio_edit_text)
        buttonSaveProfile = rootView.findViewById(R.id.save_button)

        // Load current user data
        loadUserData()

        // Enable editing on click for EditText fields
        enableEditing(firstNameEditText)
        enableEditing(lastNameEditText)
        enableEditing(bioEditText)

        // Enable changing the profile picture on click
        profileImageView.setOnClickListener {
            showPictureOptionDialog()
        }

        buttonSaveProfile.setOnClickListener {
            updateProfileData()
        }

        return rootView
    }

    private fun enableEditing(editText: EditText) {
        editText.setOnClickListener {
            editText.isFocusableInTouchMode = true
            editText.requestFocus()
        }
    }

    private fun updateProfileData() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your First Name and Last Name", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: ""
        if (userId.isNotEmpty()) {
            // Assuming profileImageUri holds the Uri of the image if updated
            val profileImageUrl = photoUri?.toString() ?: ""

            // Prepare data map for database update
            val userRef = database.getReference("Users").child(userId)
            val userMap = mapOf(
                "First Name" to firstName,
                "Last Name" to lastName,
                "bio" to bio,
                "profilePictureUrl" to profileImageUrl
            )

            userRef.updateChildren(userMap).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.getReference("Users").child(userId).get()
                .addOnSuccessListener { dataSnapshot ->
                    val firstName = dataSnapshot.child("First Name").value as? String ?: "N/A"
                    val lastName = dataSnapshot.child("Last Name").value as? String ?: "N/A"
                    val username = dataSnapshot.child("Username").value as? String ?: "N/A"
                    val bio = dataSnapshot.child("bio").value as? String ?: "N/A"
                    val profilePictureUrl = dataSnapshot.child("profilePictureUrl").value as? String

                    firstNameEditText.setText(firstName)
                    lastNameEditText.setText(lastName)
                    usernameTextView.text = username
                    bioEditText.setText(bio)

                    if (!profilePictureUrl.isNullOrEmpty()) {
                        // Only load the image if the URL is valid
                        Picasso.get().load(profilePictureUrl).placeholder(R.drawable.default_profile).into(profileImageView)
                    } else {
                        // Set a default image if the URL is null or empty
                        profileImageView.setImageResource(R.drawable.default_profile)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showPictureOptionDialog() {
        val options = arrayOf("Take Photo with Camera", "Choose from Gallery")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose an option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )
            takePictureLauncher.launch(photoUri)
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Error while creating file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(null)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            photoUri = Uri.fromFile(this)
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            profileImageView.setImageURI(photoUri)
            photoUri?.let { uri ->
                profileViewModel.setProfileImageUri(uri)
                profileViewModel.uploadProfilePicture(uri)
            }
        } else {
            Toast.makeText(requireContext(), "Camera action failed", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            profileImageView.setImageURI(selectedUri)
            photoUri = selectedUri
            profileViewModel.uploadProfilePicture(selectedUri)
        }
    }
}