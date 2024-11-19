package com.example.mainchameleon.ui.journal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mainchameleon.databinding.FragmentJournalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!
    private lateinit var journalViewModel: JournalViewModel

    private var selectedMood: String? = null // Store selected mood emoji

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)

        journalViewModel = ViewModelProvider(this).get(JournalViewModel::class.java)

        setupMoodSelection()
        setupSaveButton()

        return binding.root
    }

    private fun setupMoodSelection() {
        binding.buttonHappy.setOnClickListener { selectedMood = "😊" }
        binding.buttonSad.setOnClickListener { selectedMood = "😢" }
        binding.buttonAngry.setOnClickListener { selectedMood = "😡" }
        binding.buttonAnxious.setOnClickListener { selectedMood = "😟" }
        binding.buttonNeutral.setOnClickListener { selectedMood = "😐" }
    }

    private fun setupSaveButton() {
        binding.saveJournalButton.setOnClickListener {
            val title = binding.titleEntryBox.text.toString().trim()
            val text = binding.journalEntryText.text.toString().trim()

            if (title.isEmpty() && text.isEmpty() && selectedMood == null) {
                Toast.makeText(requireContext(), "Please provide journal details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Correctly exit the lambda
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Correctly exit the lambda
            }

            val journalEntry = JournalEntry(
                id = UUID.randomUUID().toString(),
                title = title,
                text = text,
                mood = selectedMood,
                userId = userId
            )

            FirebaseDatabase.getInstance()
                .getReference("Users/$userId/journals")
                .child(journalEntry.id)
                .setValue(journalEntry)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Journal saved!", Toast.LENGTH_SHORT).show()
                    clearForm()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to save journal", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun clearForm() {
        binding.titleEntryBox.text.clear()
        binding.journalEntryText.text.clear()
        selectedMood = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
