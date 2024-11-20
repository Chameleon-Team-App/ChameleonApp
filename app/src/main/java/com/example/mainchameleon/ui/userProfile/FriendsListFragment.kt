package com.example.mainchameleon.ui.userProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.ItemFriendBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FriendsListFragment : Fragment() {

    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_friends_lists, container, false)
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        val addFriendButton: ImageButton = view.findViewById(R.id.add_friend_button)

        // Navigate back to the user profile
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Show Add Friend dialog
        addFriendButton.setOnClickListener {
            showAddFriendDialog()
        }

        // Initialize Firebase and RecyclerView
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view)
        friendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        friendsAdapter = FriendsAdapter { friendId -> navigateToFriendProfile(friendId) }
        friendsRecyclerView.adapter = friendsAdapter

        loadFriends()

        return view
    }

    private fun loadFriends() {
        val currentUserId = auth.currentUser?.uid ?: return
        val friendsRef = database.child("Users").child(currentUserId).child("friends")

        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friends = mutableListOf<Friend>()
                if (snapshot.exists()) {
                    for (friendSnapshot in snapshot.children) {
                        val friendId = friendSnapshot.key ?: continue
                        database.child("Users").child(friendId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(friendData: DataSnapshot) {
                                val username = friendData.child("Username").value.toString()
                                val profilePictureUrl = friendData.child("profilePictureUrl").value.toString()
                                friends.add(Friend(friendId, username, profilePictureUrl))
                                friendsAdapter.submitList(friends)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(requireContext(), "Failed to load friend data", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                } else {
                    Toast.makeText(requireContext(), "No friends found", Toast.LENGTH_SHORT).show()
                    friendsAdapter.submitList(friends) // Clear the list if empty
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load friends", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun addFriend(friendId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val currentUserFriendsRef = database.child("Users").child(currentUserId).child("friends")
        val friendUserFriendsRef = database.child("Users").child(friendId).child("friends")

        database.child("Users").child(friendId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Add friendId to the current user's friends
                currentUserFriendsRef.child(friendId).setValue(true).addOnSuccessListener {
                    // Add currentUserId to the friend's friends
                    friendUserFriendsRef.child(currentUserId).setValue(true).addOnSuccessListener {
                        Toast.makeText(requireContext(), "Friend added successfully!", Toast.LENGTH_SHORT).show()
                        loadFriends() // Refresh the list
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to add to friend's list", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add friend", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Friend code not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error checking friend code", Toast.LENGTH_SHORT).show()
        }
    }



    private fun showAddFriendDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_friend, null)
        val friendCodeInput = dialogView.findViewById<EditText>(R.id.friend_code_input)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Friend")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val friendId = friendCodeInput.text.toString().trim()
                if (friendId.isNotEmpty()) {
                    addFriend(friendId)
                } else {
                    Toast.makeText(requireContext(), "Friend code cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToFriendProfile(friendId: String) {
        val bundle = Bundle().apply {
            putString("friendId", friendId)
        }
        findNavController().navigate(R.id.navigation_friend_profile, bundle)
    }
}

data class Friend(val id: String, val username: String, val profilePictureUrl: String)
