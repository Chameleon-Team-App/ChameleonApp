package com.example.mainchameleon.ui.journal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.FragmentJournalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class JournalFragment : Fragment() {

    private lateinit var binding: FragmentJournalBinding
    private lateinit var journalViewModel: JournalViewModel
    private var imageUri: Uri? = null
    private val storageRef = FirebaseStorage.getInstance().reference.child("journalImages")
    private lateinit var database: DatabaseReference
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private var photoUrl: String? = null // to store the photo URL

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJournalBinding.inflate(inflater, container, false)
        journalViewModel = ViewModelProvider(this).get(JournalViewModel::class.java)

        // Initialize Firebase database reference for the current user
        database = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser?.uid ?: "")

        // Handle photo URL returned from CameraFragment
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("photoUrl")
            ?.observe(viewLifecycleOwner) { url ->
                photoUrl = url // Store the photo URL
                binding.imageViewPlaceholder.setImageURI(Uri.parse(url)) // Display the image
            }

        binding.openCameraButton.setOnClickListener {
            findNavController().navigate(R.id.action_journalFragment_to_cameraFragment)
        }

        binding.uploadFromGalleryButton.setOnClickListener {
            openGallery()
        }

        binding.saveJournalButton.setOnClickListener {
            saveJournalEntry()
        }

        return binding.root
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                imageUri = data.data
                binding.imageViewPlaceholder.setImageURI(imageUri) // Display the selected image in the placeholder

                // Call method to upload the selected image to Firebase Storage
                uploadImageToFirebase(imageUri)
            }
        }
    }

    private fun uploadImageToFirebase(uri: Uri?) {
        if (uri == null) return
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val userPhotoRef = storageRef.child("users/${user.uid}/photos/${UUID.randomUUID()}.jpg")

            userPhotoRef.putFile(uri)
                .addOnSuccessListener {
                    userPhotoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        photoUrl = downloadUri.toString() // Save the download URL for use in the journal entry
                        Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not authenticated.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveJournalEntry() {
        val title = binding.titleEntryBox.text.toString().trim()
        val text = binding.journalEntryText.text.toString().trim()

        if (title.isEmpty() || text.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val journalEntry = JournalEntry(
            title = title,
            text = text,
            imageUrl = photoUrl // Include the photo URL if available
        )

        val noteId = database.child("journals").push().key ?: UUID.randomUUID().toString()
        database.child("journals").child(noteId).setValue(journalEntry)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(requireContext(), "Journal saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to save journal", Toast.LENGTH_SHORT).show()
                }
            }
    }



    companion object {
        private const val GALLERY_REQUEST_CODE = 1
    }
}
