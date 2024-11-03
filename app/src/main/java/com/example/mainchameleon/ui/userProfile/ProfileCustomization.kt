package com.example.mainchameleon.ui.userProfile

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import java.util.Date
import java.util.Locale

class ProfileCustomizationFragment : Fragment() {

    private lateinit var buttonChangePicture: Button
    private lateinit var buttonSaveProfile: Button
    private lateinit var profileImageView: ImageView
    private lateinit var bioEditText: EditText
    private lateinit var birthdayButton: Button
    private var photoUri: Uri? = null
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile_customization, container, false)

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        buttonChangePicture = rootView.findViewById(R.id.change_picture_button)
        buttonSaveProfile = rootView.findViewById(R.id.save_button)
        profileImageView = rootView.findViewById(R.id.profile_image_preview)
        bioEditText = rootView.findViewById(R.id.bio_edit_text)
        birthdayButton = rootView.findViewById(R.id.select_birthday_button)

        buttonChangePicture.setOnClickListener {
            showPictureOptionDialog()
        }

        birthdayButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = "$dayOfMonth/${month + 1}/$year"
                    birthdayButton.text = selectedDate
                    profileViewModel.setBirthday(selectedDate)
                },
                2000, 0, 1
            )
            datePickerDialog.show()
        }

        buttonSaveProfile.setOnClickListener {
            val bio = bioEditText.text.toString().trim()
            val birthday = birthdayButton.text.toString()

            profileViewModel.setBio(bio)
            profileViewModel.setBirthday(birthday)

            val userId = auth.currentUser?.uid ?: ""
            if (userId.isNotEmpty()) {
                val profileImageUrl = photoUri?.toString() ?: ""
                profileViewModel.saveProfileDataToDatabase(profileImageUrl, bio, birthday)
            } else {
                Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        }

        observeViewModel()

        return rootView
    }

    private fun observeViewModel() {
        profileViewModel.uploadStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                true -> Toast.makeText(requireContext(), "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show()
                false -> Toast.makeText(requireContext(), "Failed to upload profile picture", Toast.LENGTH_SHORT).show()
                else -> { /* No action needed */ }
            }
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
}
