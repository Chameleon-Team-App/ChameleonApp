package com.example.mainchameleon.ui.userProfile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileCustomizationFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var bioEditText: EditText
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var usernameEditText: EditText
    private var photoUri: Uri? = null
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile_customization, container, false)

        profileImageView = rootView.findViewById(R.id.change_picture_button)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        usernameEditText = rootView.findViewById(R.id.username_edit_text)
        firstNameEditText = rootView.findViewById(R.id.fname_edit_text)
        lastNameEditText = rootView.findViewById(R.id.lname_edit_text)
        bioEditText = rootView.findViewById(R.id.bio_edit_text)
        val saveButtonCard: MaterialCardView = rootView.findViewById(R.id.save_button)
        val backButton = rootView.findViewById<View>(R.id.back_button)

        // Load current user data
        loadUserData()

        // Enable editing for EditText fields
        enableEditing(firstNameEditText)
        enableEditing(lastNameEditText)
        enableEditing(bioEditText)
        enableEditing(usernameEditText)

        // Enable profile picture change
        profileImageView.setOnClickListener {
            showPictureOptionDialog()
        }

        // Save profile data
        saveButtonCard.setOnClickListener {
            updateProfileData()
        }

        // Back button functionality
        backButton.setOnClickListener {
            navigateBack()
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
        val username = usernameEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty()) {
            Toast.makeText(requireContext(), "First Name, Last Name, and Username cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("Users").child(userId)

        val userMap = mutableMapOf<String, Any>(
            "First Name" to firstName,
            "Last Name" to lastName,
            "Username" to username,
            "bio" to bio
        )

        // Add profilePictureUrl only if photoUri is non-null
        photoUri?.let {
            profileViewModel.uploadProfilePicture(it) // Upload the picture when saving
            userMap["profilePictureUrl"] = it.toString()
        }

        // Update the database
        userRef.updateChildren(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to update profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.getReference("Users").child(userId).get()
                .addOnSuccessListener { dataSnapshot ->
                    val firstName = dataSnapshot.child("First Name").value as? String ?: ""
                    val lastName = dataSnapshot.child("Last Name").value as? String ?: ""
                    val username = dataSnapshot.child("Username").value as? String ?: ""
                    val bio = dataSnapshot.child("bio").value as? String ?: ""
                    val profilePictureUrl = dataSnapshot.child("profilePictureUrl").value as? String

                    firstNameEditText.setText(firstName)
                    lastNameEditText.setText(lastName)
                    usernameEditText.setText(username)
                    bioEditText.setText(bio)

                    if (!profilePictureUrl.isNullOrEmpty()) {
                        Picasso.get().load(profilePictureUrl)
                            .placeholder(R.drawable.default_profile)
                            .into(profileImageView)
                    } else {
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
        } else {
            Toast.makeText(requireContext(), "Camera action failed", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            profileImageView.setImageURI(selectedUri)
            photoUri = selectedUri
        }
    }

    private fun navigateBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}
