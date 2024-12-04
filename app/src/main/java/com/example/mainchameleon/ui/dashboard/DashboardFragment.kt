package com.example.mainchameleon.ui.dashboard

import android.content.Intent
import android.net.Uri
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
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.FragmentDashboardBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var dashboardViewModel: DashboardViewModel

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

        // Initialize profile views
        setupProfileViews()

        // Initialize ViewModel
        initializeViewModel()

        // Observe most recent journal
        observeMostRecentJournal()

        // Load user profile data
        loadUserProfile()

        // Set up swipe-to-refresh
        setupSwipeToRefresh()

        // Set navigation button listeners
        setupNavigationButtons()

        return binding.root
    }

    private fun setupProfileViews() {
        profileImageView = binding.root.findViewById(R.id.profile_image)
        userNameTextView = binding.root.findViewById(R.id.user_name)
        fullNameTextView = binding.root.findViewById(R.id.fullName)
        streakTextView = binding.root.findViewById(R.id.streakTextView)
    }

    private fun initializeViewModel(): ConstraintLayout {
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        dashboardViewModel.streak.observe(viewLifecycleOwner) { streak ->
            streakTextView.text = if (streak.currentStreak > 0) "🔥 ${streak.currentStreak}" else "🔥 0"
        }

        dashboardViewModel.updateStreak()

        return binding.root
    }

    private fun observeMostRecentJournal() {
        dashboardViewModel.getMostRecentJournal().observe(viewLifecycleOwner) { journal ->
            val recentJournalCard = binding.recentJournalCard
            if (journal != null) {
                recentJournalCard.visibility = View.VISIBLE

                // Set title, text, and mood
                binding.recentJournalTitle.text = journal.title
                binding.recentJournalText.text = journal.text
                binding.recentJournalMood.text = journal.mood ?: ""

                // Set background color
                try {
                    binding.recentJournalCard.setCardBackgroundColor(journal.backgroundColor)
                } catch (e: Exception) {
                    binding.recentJournalCard.setCardBackgroundColor(
                        requireContext().getColor(R.color.default_background)
                    )
                }

                // Set date
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                binding.recentJournalDate.text = try {
                    val date = Date(journal.timestamp)
                    "Date: ${dateFormat.format(date)}"
                } catch (e: Exception) {
                    "Date: Unknown"
                }

                // Load image
                if (!journal.imageUrl.isNullOrEmpty()) {
                    binding.recentJournalImage.visibility = View.VISIBLE
                    Picasso.get().load(journal.imageUrl).into(binding.recentJournalImage)
                } else {
                    binding.recentJournalImage.visibility = View.GONE
                }

                // Fetch user details (username and profile picture)
                val userRef = dashboardViewModel.getUserReference(journal.userId)

                userRef.child("Username").get().addOnSuccessListener { snapshot ->
                    binding.recentJournalUsername.text =
                        snapshot.getValue(String::class.java) ?: "Unknown User"
                }.addOnFailureListener {
                    binding.recentJournalUsername.text = "Unknown User"
                }

                userRef.child("profilePictureUrl").get().addOnSuccessListener { snapshot ->
                    val profilePictureUrl = snapshot.getValue(String::class.java)
                    if (!profilePictureUrl.isNullOrEmpty()) {
                        Picasso.get()
                            .load(profilePictureUrl)
                            .placeholder(R.drawable.default_profile)
                            .error(R.drawable.default_profile)
                            .into(binding.profileImageView)
                    } else {
                        binding.profileImageView.setImageResource(R.drawable.default_profile)
                    }
                }.addOnFailureListener {
                    binding.profileImageView.setImageResource(R.drawable.default_profile)
                }

                recentJournalCard.setOnClickListener {
                    findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_journal)
                }
            } else {
                recentJournalCard.visibility = View.GONE
            }
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            dashboardViewModel.updateStreak()
        }
    }

    private fun setupNavigationButtons() {
        binding.JournalButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_journal)
        }

        profileImageView.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_profile)
        }

        binding.MentalHealthButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_calendar)
        }

        // Add Find a Specialist Button click listener
        binding.FindSpecialistButton.setOnClickListener {
            val url = "https://www.betterhelp.com/"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

    private fun loadUserProfile() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Users").child(userId)

        userRef.child("Username").get().addOnSuccessListener { dataSnapshot ->
            userNameTextView.text = dataSnapshot.getValue(String::class.java) ?: "Unknown User"
        }

        userRef.child("First Name").get().addOnSuccessListener { dataSnapshot ->
            val firstName = dataSnapshot.getValue(String::class.java) ?: ""
            userRef.child("Last Name").get().addOnSuccessListener { lastSnapshot ->
                val lastName = lastSnapshot.getValue(String::class.java) ?: ""
                fullNameTextView.text = "$firstName $lastName"
            }
        }

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
