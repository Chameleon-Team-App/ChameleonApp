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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*

class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!
    private lateinit var journalViewModel: JournalViewModel

    private var imageUri: Uri? = null
    private var imageUrl: String? = null
    private var selectedMood: String? = null

    companion object {
        private const val REQUEST_IMAGE_PICK = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        journalViewModel = ViewModelProvider(this).get(JournalViewModel::class.java)

        setupMoodButtons()
        setupImageButtons()
        setupSaveButton()
        setupBackButton()

        // Display the most recent journal entry
        displayMostRecentJournal()

        return binding.root
    }

    private fun displayMostRecentJournal() {
        val mostRecentJournal = journalViewModel.getMostRecentJournal()
        if (mostRecentJournal != null) {
            binding.titleEntryBox.setText(mostRecentJournal.title)
            binding.journalEntryText.setText(mostRecentJournal.text)
            selectedMood = mostRecentJournal.mood

            // Update mood buttons
            when (selectedMood) {
                "😊" -> binding.buttonHappy.isSelected = true
                "😢" -> binding.buttonSad.isSelected = true
                "😡" -> binding.buttonAngry.isSelected = true
                "😟" -> binding.buttonAnxious.isSelected = true
                "😐" -> binding.buttonNeutral.isSelected = true
            }

            // Display image if present
            if (!mostRecentJournal.imageUrl.isNullOrEmpty()) {
                Picasso.get().load(mostRecentJournal.imageUrl).into(binding.imageViewPlaceholder)
                binding.imageViewPlaceholder.visibility = View.VISIBLE
            }
        } else {
            Toast.makeText(requireContext(), "No recent journal entries found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMoodButtons() {
        binding.buttonHappy.setOnClickListener { selectedMood = "😊" }
        binding.buttonSad.setOnClickListener { selectedMood = "😢" }
        binding.buttonAngry.setOnClickListener { selectedMood = "😡" }
        binding.buttonAnxious.setOnClickListener { selectedMood = "😟" }
        binding.buttonNeutral.setOnClickListener { selectedMood = "😐" }
    }

    private fun setupImageButtons() {
        binding.openCameraButton.setOnClickListener {
            val bundle = Bundle().apply { putString("source", "journal") }
            findNavController().navigate(R.id.action_navigation_journal_to_navigation_camera, bundle)
        }

        binding.uploadFromGalleryButton.setOnClickListener {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    imageUri = data?.data
                    imageUrl = null
                    binding.imageViewPlaceholder.setImageURI(imageUri)
                    binding.imageViewPlaceholder.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupSaveButton() {
        binding.saveJournalButton.setOnClickListener {
            val title = binding.titleEntryBox.text.toString().trim()
            val text = binding.journalEntryText.text.toString().trim()

            if (title.isEmpty() && text.isEmpty() && selectedMood == null && imageUri == null && imageUrl == null) {
                Toast.makeText(requireContext(), "Please fill in the journal details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val journalId = UUID.randomUUID().toString()

            val journalEntry = JournalEntry(
                id = journalId,
                title = title,
                text = text,
                mood = selectedMood,
                userId = userId,
                timestamp = System.currentTimeMillis()
            )

            when {
                imageUrl != null -> {
                    journalEntry.imageUrl = imageUrl
                    saveJournalToDatabase(journalEntry)
                }
                imageUri != null -> {
                    uploadImageToFirebaseStorage(journalId) { uploadedImageUrl ->
                        journalEntry.imageUrl = uploadedImageUrl
                        saveJournalToDatabase(journalEntry)
                    }
                }
                else -> saveJournalToDatabase(journalEntry)
            }
        }
    }

    private fun uploadImageToFirebaseStorage(journalId: String, callback: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().getReference("journal_images/$journalId.jpg")
        imageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        callback(downloadUri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        } ?: Toast.makeText(requireContext(), "No image to upload", Toast.LENGTH_SHORT).show()
    }

    private fun saveJournalToDatabase(journalEntry: JournalEntry) {
        val userId = journalEntry.userId
        FirebaseDatabase.getInstance()
            .getReference("Users/$userId/journals")
            .child(journalEntry.id)
            .setValue(journalEntry)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Journal saved!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_navigation_journal_to_navigation_dashboard)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save journal", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_journal_to_navigation_dashboard)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
