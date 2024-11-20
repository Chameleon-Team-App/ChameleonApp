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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
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
    private var photoUri: Uri? = null // Temporary storage for selected photo
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var usernameTextView: TextView
    private var selectedProfilePictureUri: Uri? = null // Final URI for updating profile

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

        // Save profile data when the "Save" button is clicked
        buttonSaveProfile.setOnClickListener {
            updateProfileData()
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
            val userRef = database.getReference("Users").child(userId)

            // Prepare data map for database update
            val userMap = mutableMapOf<String, Any>(
                "First Name" to firstName,
                "Last Name" to lastName,
                "bio" to bio
            )

            // Add profile picture URL if available
            selectedProfilePictureUri?.let { uri ->
                val storageRef = FirebaseStorage.getInstance().getReference("users/profilePictures/$userId.jpg")
                storageRef.putFile(uri)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            userMap["profilePictureUrl"] = downloadUri.toString()
                            userRef.updateChildren(userMap).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to upload profile picture", Toast.LENGTH_SHORT).show()
                    }
            } ?: userRef.updateChildren(userMap).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
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
            photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", photoFile)
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
            selectedProfilePictureUri = photoUri
            profileImageView.setImageURI(photoUri)
        } else {
            Toast.makeText(requireContext(), "Camera action failed", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            selectedProfilePictureUri = selectedUri
            profileImageView.setImageURI(selectedUri)
        } ?: Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
    }

    private fun navigateBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}
