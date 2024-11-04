package com.example.mainchameleon.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.FragmentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var dashboardAdapter: DashboardAdapter

    // Profile views
    private lateinit var profileImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var fullNameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize the ViewModel
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        // Initialize RecyclerView adapter
        dashboardAdapter = DashboardAdapter()
        binding.recyclerView.adapter = dashboardAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observe the allEntries LiveData from the ViewModel
        dashboardViewModel.allEntries.observe(viewLifecycleOwner) { entries ->
            dashboardAdapter.submitList(entries)
        }

        // Set click listeners for navigation buttons
        binding.JournalButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_journal)
        }
        binding.moodJournalButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
        }

        // Initialize profile views
        profileImageView = binding.profileImage // Ensure this ID matches your XML
        userNameTextView = binding.userName // Ensure this ID matches your XML
        fullNameTextView = binding.fullName // Ensure this ID matches your XML

        // Load user profile data
        loadUserProfile()

        return view
    }

    private fun loadUserProfile() {
        // Get the current user ID from Firebase Authentication
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            // Retrieve username
            userRef.child("Username").get().addOnSuccessListener { dataSnapshot ->
                val username = dataSnapshot.getValue(String::class.java)
                userNameTextView.text = username ?: "Unknown User"
            }.addOnFailureListener {
                userNameTextView.text = "Error Loading User"
            }

            // Initialize variables to hold first and last name
            var firstName: String? = null
            var lastName: String? = null

            // Retrieve first name
            userRef.child("firstName").get().addOnSuccessListener { dataSnapshot ->
                firstName = dataSnapshot.getValue(String::class.java)
                // Update full name if last name is already retrieved
                updateFullName(firstName, lastName)
            }.addOnFailureListener {
                fullNameTextView.text = "Unknown First Name"
            }

            // Retrieve last name
            userRef.child("lastName").get().addOnSuccessListener { dataSnapshot ->
                lastName = dataSnapshot.getValue(String::class.java)
                // Update full name if first name is already retrieved
                updateFullName(firstName, lastName)
            }.addOnFailureListener {
                fullNameTextView.text = "Unknown Last Name"
            }

            // Retrieve profile picture URL
            userRef.child("profilePictureUrl").get().addOnSuccessListener { dataSnapshot ->
                val profilePictureUrl = dataSnapshot.getValue(String::class.java)
                if (!profilePictureUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(profilePictureUrl)
                        .placeholder(R.drawable.default_profile) // Placeholder image
                        .error(R.drawable.default_profile) // Error image
                        .into(profileImageView)
                } else {
                    profileImageView.setImageResource(R.drawable.default_profile)
                }
            }.addOnFailureListener {
                profileImageView.setImageResource(R.drawable.default_profile)
            }
        } else {
            // Handle case where user ID is null
            userNameTextView.text = "No User Logged In"
            fullNameTextView.text = ""
            profileImageView.setImageResource(R.drawable.default_profile)
        }
    }

    // Function to update the full name once both parts are retrieved
    private fun updateFullName(firstName: String?, lastName: String?) {
        fullNameTextView.text = when {
            firstName != null && lastName != null -> "$firstName $lastName"
            firstName != null -> firstName
            lastName != null -> lastName
            else -> "Unknown Full Name"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
