package com.example.mainchameleon.ui.userProfile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mainchameleon.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ProfileCustomizationFragment : Fragment() {

    private lateinit var buttonSaveProfile: Button
    private lateinit var profileImageView: ImageView
    private lateinit var bioEditText: EditText
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var passwordEditText: EditText // New Password EditText
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

        profileImageView = rootView.findViewById(R.id.change_picture_button)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        usernameTextView = rootView.findViewById(R.id.username_edit_text)
        firstNameEditText = rootView.findViewById(R.id.fname_edit_text)
        lastNameEditText = rootView.findViewById(R.id.lname_edit_text)
        bioEditText = rootView.findViewById(R.id.bio_edit_text)
        passwordEditText = rootView.findViewById(R.id.password_edit_text) // Initialize password field
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
            handleSaveProfile()
        }

        val backButton: ImageButton = rootView.findViewById(R.id.back_button)
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

    private fun handleSaveProfile() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim()
        val newPassword = passwordEditText.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your First Name and Last Name", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.isNotEmpty()) {
            // Prompt for current password if a new password is provided
            promptForCurrentPassword { currentPassword ->
                reauthenticateUser(currentPassword, newPassword) {
                    updateProfile(firstName, lastName, bio)
                }
            }
        } else {
            // Proceed with profile update if no password change
            updateProfile(firstName, lastName, bio)
        }
    }

    private fun promptForCurrentPassword(onPasswordEntered: (String) -> Unit) {
        val input = EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Enter Current Password"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Re-authenticate")
            .setMessage("Please enter your current password to proceed.")
            .setView(input)
            .setPositiveButton("Submit") { _, _ ->
                val currentPassword = input.text.toString().trim()
                if (currentPassword.isNotEmpty()) {
                    onPasswordEntered(currentPassword)
                } else {
                    Toast.makeText(requireContext(), "Password is required to proceed.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun reauthenticateUser(currentPassword: String, newPassword: String, onReauthenticated: () -> Unit) {
        val user = auth.currentUser
        val email = user?.email ?: return

        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updatePassword(newPassword, onReauthenticated)
                } else {
                    Toast.makeText(requireContext(), "Re-authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updatePassword(newPassword: String, onPasswordUpdated: () -> Unit) {
        val user = auth.currentUser

        user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show()
                onPasswordUpdated()
            } else {
                Toast.makeText(requireContext(), "Password update failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProfile(firstName: String, lastName: String, bio: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("Users").child(userId)

        val userMap = mapOf(
            "First Name" to firstName,
            "Last Name" to lastName,
            "bio" to bio,
            "profilePictureUrl" to (photoUri?.toString() ?: "")
        )

        userRef.updateChildren(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
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
                        Picasso.get().load(profilePictureUrl).placeholder(R.drawable.default_profile).into(profileImageView)
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

    private fun navigateBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
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