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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJournalBinding.inflate(inflater, container, false)
        journalViewModel = ViewModelProvider(this).get(JournalViewModel::class.java)

        // Initialize Firebase database reference for the current user
        database = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser?.uid ?: "")

        // Handle photo returned from CameraFragment
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("photoUri")
            ?.observe(viewLifecycleOwner) { photoUri ->
                val uri = Uri.parse(photoUri)
                imageUri = uri
                binding.imageViewPlaceholder.setImageURI(uri)
            }

        // Button to open the camera
        binding.openCameraButton.setOnClickListener {
            findNavController().navigate(R.id.action_journalFragment_to_cameraFragment)
        }

        // Button to open gallery
        binding.uploadFromGalleryButton.setOnClickListener {
            openGallery()
        }

        // Button to save the journal entry
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
                binding.imageViewPlaceholder.setImageURI(imageUri)
            }
        }
    }

    private fun saveJournalEntry() {
        val title = binding.titleEntryBox.text.toString().trim()
        val text = binding.journalEntryText.text.toString().trim()

        if (title.isEmpty() || text.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            // Upload image to Firebase Storage
            val imageRef = storageRef.child(UUID.randomUUID().toString())
            imageRef.putFile(imageUri!!).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveNoteToDatabase(title, text, uri.toString())
                }
            }
        } else {
            // Save entry without an image
            saveNoteToDatabase(title, text, null)
        }
    }

    private fun saveNoteToDatabase(title: String, text: String, imageUrl: String?) {
        val noteId = database.child("journals").push().key ?: UUID.randomUUID().toString()
        val journalEntry = JournalEntry(
            title = title,
            text = text,
            imageUrl = imageUrl
        )

        // Save the journal entry under the user's "journals" node
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
