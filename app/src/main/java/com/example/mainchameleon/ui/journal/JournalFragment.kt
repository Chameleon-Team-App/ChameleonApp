// JournalFragment.kt
package com.example.mainchameleon.ui.journal

import android.app.Activity
import android.content.Context
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
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!
    private lateinit var journalViewModel: JournalViewModel

    private var imageUri: Uri? = null
    private var imageUrl: String? = null
    private var selectedMood: String? = null
    private var journalMap: MutableMap<String, Boolean> = mutableMapOf()


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

        // Observe for photo URL passed from CameraFragment
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("photoUrl")
            ?.observe(viewLifecycleOwner) { photoUrl ->
                imageUrl = photoUrl
                // Show the image in the placeholder
                binding.imageViewPlaceholder.visibility = View.VISIBLE
                Picasso.get().load(photoUrl).into(binding.imageViewPlaceholder)
            }

        return binding.root
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
            findNavController().navigate(R.id.action_navigation_journal_to_navigation_camera)
        }

        binding.uploadFromGalleryButton.setOnClickListener {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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
        binding.saveButton.setOnClickListener {
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
                imageUrl = imageUrl,
                userId = userId,
                timestamp = System.currentTimeMillis()
            )

            if (imageUri != null) {
                uploadImageToFirebaseStorage(journalId) { uploadedImageUrl ->
                    journalEntry.imageUrl = uploadedImageUrl
                    saveJournalToDatabase(journalEntry)
                }
            } else {
                saveJournalToDatabase(journalEntry)
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
        val formattedDate = LocalDate.ofEpochDay(journalEntry.timestamp / 86400000L)
            .format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))

        FirebaseDatabase.getInstance()
            .getReference("Users/$userId/journals")
            .child(journalEntry.id)
            .setValue(journalEntry)
            .addOnSuccessListener {
                // Update journal map for the heatmap
                val sharedPreferences = requireContext().getSharedPreferences("journals", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                journalMap[formattedDate] = true
                val journalJson = Gson().toJson(journalMap)
                editor.putString("journal_map", journalJson)
                editor.apply()

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
