package com.example.mainchameleon.ui.userProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.FragmentUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        loadUserProfile()

        return binding.root
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            database.child("Users").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val username = snapshot.child("Username").value?.toString() ?: "Unknown"
                        val bio = snapshot.child("bio").value?.toString() ?: "No bio available"
                        val profilePictureUrl = snapshot.child("profilePictureUrl").value?.toString() ?: ""

                        binding.usernameText.text = username
                        binding.bioText.text = bio
                        binding.friendCodeText.text = "Friend Code: $userId"

                        // Load profile picture if available
                        if (profilePictureUrl.isNotEmpty()) {
                            Picasso.get().load(profilePictureUrl)
                                .placeholder(R.drawable.default_profile)
                                .into(binding.profileImage)
                        } else {
                            binding.profileImage.setImageResource(R.drawable.default_profile)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Failed to load user profile", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            // Navigate back or handle unauthenticated state
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
