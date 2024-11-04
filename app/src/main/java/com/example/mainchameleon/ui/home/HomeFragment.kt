package com.example.mainchameleon.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.FragmentHomeBinding
import com.example.mainchameleon.ui.journal.JournalAdapter
import com.example.mainchameleon.ui.mood.MoodEntry
import com.example.mainchameleon.ui.journal.JournalViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var selectedMood: String? = null
    private val moodButtons = mutableListOf<Button>()

    private lateinit var journalViewModel: JournalViewModel
    private lateinit var moodAdapter: JournalAdapter // Alternatively, create a MoodAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard)
        }

        // Initialize mood buttons
        moodButtons.add(binding.buttonHappy)
        moodButtons.add(binding.buttonSad)
        moodButtons.add(binding.buttonAngry)
        moodButtons.add(binding.buttonAnxious)
        moodButtons.add(binding.buttonNeutral)

        // Set click listeners for mood buttons
        for (button in moodButtons) {
            button.setOnClickListener {
                onMoodSelected(button)
            }
        }

        // Handle submit button click
        binding.submitMoodButton.setOnClickListener {
            saveMoodEntry()
        }

        // Initialize the ViewModel
        journalViewModel = ViewModelProvider(this).get(JournalViewModel::class.java)

        // Initialize RecyclerView adapter for mood entries
        moodAdapter = JournalAdapter() // Alternatively, use a separate MoodAdapter
        binding.moodRecyclerView.adapter = moodAdapter
        binding.moodRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observe the currentUserMoodEntries LiveData from the ViewModel
        journalViewModel.currentUserMoodEntries.observe(viewLifecycleOwner) { entries ->
            moodAdapter.submitList(entries)
        }

        return root
    }

    private fun onMoodSelected(button: Button) {
        // Reset background of all buttons
        for (btn in moodButtons) {
            btn.setBackgroundColor(resources.getColor(android.R.color.transparent))
        }
        // Set background of selected button
        button.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
        // Store selected mood
        selectedMood = button.text.toString()
    }

    private fun saveMoodEntry() {
        val sentence = binding.moodSentence.text.toString().trim()

        if (selectedMood == null || sentence.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a mood and write a sentence", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().reference
        val noteId = database.child("Users").child(userId).child("moods").push().key
            ?: UUID.randomUUID().toString()

        val moodEntry = MoodEntry(
            id = noteId,
            mood = selectedMood!!,
            sentence = sentence,
            backgroundColor = MoodEntry.generateRandomColor(),
            userId = userId
        )

        database.child("Users").child(userId).child("moods").child(noteId).setValue(moodEntry)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Mood entry saved", Toast.LENGTH_SHORT).show()
                    clearMoodForm()
                } else {
                    Toast.makeText(requireContext(), "Failed to save mood entry", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun clearMoodForm() {
        binding.moodSentence.text.clear()
        selectedMood = null
        // Reset background of all buttons
        for (btn in moodButtons) {
            btn.setBackgroundColor(resources.getColor(android.R.color.transparent))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
