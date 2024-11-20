package com.example.mainchameleon.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.FragmentDashboardBinding
import com.example.mainchameleon.ui.calendar.WeeklyCalendarAdapter
import com.squareup.picasso.Picasso
import java.util.Date

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
        setupDashboardRecyclerView()

        // Initialize Weekly Calendar RecyclerView
        setupWeeklyCalendar()

        // Initialize profile views
        setupProfileViews()

        // Initialize ViewModel
        initializeViewModel()

        // Load user profile data
        loadUserProfile()

        // Set up swipe-to-refresh
        setupSwipeToRefresh()

        // Set navigation button listeners
        setupNavigationButtons()

        return binding.root
    }

    private fun setupDashboardRecyclerView() {
        dashboardAdapter = DashboardAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dashboardAdapter
        }
    }

    private fun setupWeeklyCalendar() {
        val currentDate = Date()
        val weeklyCalendarAdapter = WeeklyCalendarAdapter(requireContext(), currentDate) { selectedDate ->
            // Handle click on a date in the weekly calendar
            navigateToCalendarFragment(selectedDate)
        }
        binding.weeklyCalendarRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = weeklyCalendarAdapter
        }
    }

    private fun setupProfileViews() {
        profileImageView = binding.root.findViewById(R.id.profile_image)
        userNameTextView = binding.root.findViewById(R.id.user_name)
        fullNameTextView = binding.root.findViewById(R.id.fullName)
        streakTextView = binding.root.findViewById(R.id.streakTextView)
    }

    private fun initializeViewModel(): ConstraintLayout {
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        dashboardViewModel.allEntries.observe(viewLifecycleOwner) { entries ->
            dashboardAdapter.submitList(entries.sortedByDescending { it.timestamp }) // Ensure newest entries appear first
            binding.swipeRefreshLayout.isRefreshing = false // Stop the refresh animation
        }

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
        // Existing Journal button
        binding.root.findViewById<View>(R.id.JournalButton).setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_journal)
        }

        // Existing Profile button
        profileImageView.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_profile)
        }

        // New Mental Health button
        binding.root.findViewById<View>(R.id.MentalHealthButton).setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_mental_health)
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

    private fun navigateToCalendarFragment(selectedDate: Date) {
        // Add logic for navigating or passing data to CalendarFragment
        findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_calendar)
        Toast.makeText(requireContext(), "Selected Date: $selectedDate", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}