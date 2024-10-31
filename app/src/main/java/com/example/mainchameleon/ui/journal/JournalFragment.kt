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
import com.example.mainchameleon.databinding.FragmentJournalBinding
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class JournalFragment : Fragment() {

    private lateinit var binding: FragmentJournalBinding
    private lateinit var journalViewModel: JournalViewModel
    private var imageUri: Uri? = null
    private val storageRef = FirebaseStorage.getInstance().reference.child("journalImages")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJournalBinding.inflate(inflater, container, false)
        journalViewModel = ViewModelProvider(this).get(JournalViewModel::class.java)

        // Button to open gallery
        binding.uploadFromGalleryButton.setOnClickListener {
            openGallery()
        }

        // Button to open camera
        binding.openCameraButton.setOnClickListener {
            openCamera()
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

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                imageUri = data.data
            } else if (requestCode == CAMERA_REQUEST_CODE && data != null) {
                imageUri = data.extras?.get("data") as Uri
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
            // Upload image to Firebase Storage if image is selected
            val imageRef = storageRef.child(UUID.randomUUID().toString())
            imageRef.putFile(imageUri!!).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val journalEntry = JournalEntry(
                        title = title,
                        text = text,
                        imageUrl = uri.toString()
                    )
                    journalViewModel.saveJournalEntry(journalEntry)
                    Toast.makeText(requireContext(), "Journal saved with image", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Save entry without an image
            val journalEntry = JournalEntry(
                title = title,
                text = text
            )
            journalViewModel.saveJournalEntry(journalEntry)
            Toast.makeText(requireContext(), "Journal saved without image", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 1
        private const val CAMERA_REQUEST_CODE = 2
    }
}
