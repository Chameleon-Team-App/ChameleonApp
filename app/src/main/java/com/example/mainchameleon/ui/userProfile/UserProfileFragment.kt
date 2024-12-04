package com.example.mainchameleon.ui.userProfile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mainchameleon.LoginActivity
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.FragmentUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class UserProfileFragment : Fragment() {

    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize View Binding
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        setupButtons()
        loadUserProfile()

        return binding.root
    }

    private fun setupButtons() {
        // Logout Button
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            navigateToLoginScreen()
        }

        // Edit Profile Button
        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_navigation_profile_customization)
        }

        // Back Button
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        database.getReference("Users").child(userId).get().addOnSuccessListener { dataSnapshot ->
            val profilePictureUrl = dataSnapshot.child("profilePictureUrl").value as? String
            val username = dataSnapshot.child("Username").value as? String
            val bio = dataSnapshot.child("bio").value as? String

            // Set profile picture with a fallback for empty or null URL
            if (!profilePictureUrl.isNullOrEmpty()) {
                Picasso.get().load(profilePictureUrl).placeholder(R.drawable.default_profile).into(binding.profileImage)
            } else {
                binding.profileImage.setImageResource(R.drawable.default_profile)
            }

            binding.usernameText.text = username ?: "N/A"
            binding.bioText.text = bio ?: "N/A" // Ensure this displays the bio
        }.addOnFailureListener {
            // Handle any errors, such as network failure or database issues
            binding.profileImage.setImageResource(R.drawable.default_profile)
            Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
        }
    }
}