package com.example.mainchameleon.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.FragmentDashboardBinding
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
    private lateinit var streakTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        // Initialize RecyclerView adapter
        dashboardAdapter = DashboardAdapter()
        binding.recyclerView.adapter = dashboardAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize profile views
        profileImageView = binding.root.findViewById(R.id.profile_image)
        userNameTextView = binding.root.findViewById(R.id.user_name)
        fullNameTextView = binding.root.findViewById(R.id.fullName)
        streakTextView = binding.root.findViewById(R.id.streakTextView)

        // Initialize the ViewModel
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        // Observe journal entries
        dashboardViewModel.allEntries.observe(viewLifecycleOwner) { entries ->
            dashboardAdapter.submitList(entries.sortedByDescending { it.timestamp }) // Ensure newest entries appear first
            binding.swipeRefreshLayout.isRefreshing = false // Stop the refresh animation
        }

        // Observe streak updates
        dashboardViewModel.streak.observe(viewLifecycleOwner) { streak ->
            streakTextView.text = if (streak.currentStreak > 0) "🔥 ${streak.currentStreak}" else "🔥 0"
        }

        // Load user profile data
        loadUserProfile()

        // Set up swipe-to-refresh
        setupSwipeToRefresh()

        // Set click listeners for navigation buttons
        setupNavigationButtons()

        // Update streak when the fragment is created
        dashboardViewModel.updateStreak()

        return binding.root
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            dashboardViewModel.loadAllEntries()
            dashboardViewModel.updateStreak()
        }
    }

    private fun setupNavigationButtons() {
        // Navigate to JournalFragment
        binding.root.findViewById<View>(R.id.JournalButton).setOnClickListener { view ->
            view.findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_journal)
        }

        // Navigate to UserProfileFragment
        profileImageView.setOnClickListener { view ->
            view.findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_profile)
        }
    }

    private fun loadUserProfile() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Users").child(userId)

        // Retrieve and set username
        userRef.child("Username").get().addOnSuccessListener { dataSnapshot ->
            userNameTextView.text = dataSnapshot.getValue(String::class.java) ?: "Unknown User"
        }

        // Retrieve and set full name
        userRef.child("First Name").get().addOnSuccessListener { dataSnapshot ->
            val firstName = dataSnapshot.getValue(String::class.java) ?: ""
            userRef.child("Last Name").get().addOnSuccessListener { lastSnapshot ->
                val lastName = lastSnapshot.getValue(String::class.java) ?: ""
                fullNameTextView.text = "$firstName $lastName"
            }
        }

        // Retrieve and set profile picture
        userRef.child("profilePictureUrl").get().addOnSuccessListener { dataSnapshot ->
            val profilePictureUrl = dataSnapshot.getValue(String::class.java)
            if (!profilePictureUrl.isNullOrEmpty()) {
                Picasso.get().load(profilePictureUrl).into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.default_profile)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
