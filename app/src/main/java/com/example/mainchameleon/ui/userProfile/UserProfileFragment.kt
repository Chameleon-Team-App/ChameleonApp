package com.example.mainchameleon.ui.userProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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

        loadUserProfile()

        return binding.root
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        database.getReference("Users").child(userId).get().addOnSuccessListener { dataSnapshot ->
            val profilePictureUrl = dataSnapshot.child("profilePictureUrl").value as? String
            val username = dataSnapshot.child("Username").value as? String
            val firstName = dataSnapshot.child("firstName").value as? String
            val lastName = dataSnapshot.child("lastName").value as? String
            val birthday = dataSnapshot.child("birthday").value as? String

            profilePictureUrl?.let {
                Picasso.get().load(it).placeholder(R.drawable.default_profile).into(binding.profileImage)
            }

            binding.usernameText.text = username ?: "N/A"
            binding.firstNameText.text = firstName ?: "N/A"
            binding.lastNameText.text = lastName ?: "N/A"
            binding.birthdayText.text = birthday ?: "Not provided"
        }.addOnFailureListener {
            // Handle any errors
        }
    }
}
