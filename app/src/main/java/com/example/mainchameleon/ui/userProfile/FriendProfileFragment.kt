package com.example.mainchameleon.ui.userProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.FragmentFriendProfileBinding
import com.example.mainchameleon.ui.journal.JournalAdapter
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class FriendProfileFragment : Fragment() {

    private lateinit var binding: FragmentFriendProfileBinding
    private lateinit var database: DatabaseReference
    private var friendId: String? = null
    private lateinit var journalAdapter: JournalAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendProfileBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference

        // Retrieve friendId from arguments
        friendId = arguments?.getString("friendId")

        if (friendId == null) {
            Toast.makeText(requireContext(), "Friend ID is missing", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
            return binding.root
        }

        // Set up RecyclerView for journals
        journalAdapter = JournalAdapter()
        binding.journalRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.journalRecyclerView.adapter = journalAdapter

        // Set up back button
        val backButton: ImageButton = binding.root.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        loadFriendProfile()
        loadFriendJournals()

        return binding.root
    }

    private fun loadFriendProfile() {
        friendId?.let { id ->
            database.child("Users").child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("Username").value?.toString() ?: "Unknown"
                    val bio = snapshot.child("bio").value?.toString() ?: "No bio available"
                    val profilePictureUrl = snapshot.child("profilePictureUrl").value?.toString() ?: ""

                    binding.usernameText.text = username
                    binding.bioText.text = bio

                    // Load profile picture
                    if (profilePictureUrl.isNotEmpty()) {
                        Picasso.get().load(profilePictureUrl).placeholder(R.drawable.default_profile).into(binding.profileImage)
                    } else {
                        binding.profileImage.setImageResource(R.drawable.default_profile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun loadFriendJournals() {
        friendId?.let { id ->
            database.child("Users").child(id).child("journals").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val journals = mutableListOf<com.example.mainchameleon.ui.journal.JournalEntry>()
                    for (journalSnapshot in snapshot.children) {
                        val journal = journalSnapshot.getValue(com.example.mainchameleon.ui.journal.JournalEntry::class.java)
                        journal?.let { journals.add(it) }
                    }
                    journalAdapter.submitList(journals.sortedByDescending { it.timestamp })
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load journals", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
