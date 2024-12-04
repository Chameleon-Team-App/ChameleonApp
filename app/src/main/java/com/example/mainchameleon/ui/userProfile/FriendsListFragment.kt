package com.example.mainchameleon.ui.userProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FriendsListFragment : Fragment() {

    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var addFriendInput: EditText
    private lateinit var addFriendButton: Button
    private lateinit var backButton: Button
    private lateinit var feedButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_friends_lists, container, false)

        // Initialize Firebase and UI elements
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view)
        addFriendInput = view.findViewById(R.id.add_friend_input)
        addFriendButton = view.findViewById(R.id.add_friend_button)
        backButton = view.findViewById(R.id.back_button)
        feedButton = view.findViewById(R.id.feed_button)

        // Set up RecyclerView
        friendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        friendsAdapter = FriendsAdapter { friendId -> navigateToFriendProfile(friendId) }
        friendsRecyclerView.adapter = friendsAdapter

        // Load existing friends
        loadFriends()

        // Handle adding a new friend
        addFriendButton.setOnClickListener {
            val friendCode = addFriendInput.text.toString().trim()
            if (friendCode.isNotEmpty()) {
                addFriend(friendCode)
            } else {
                Toast.makeText(requireContext(), "Friend code cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle back button to navigate to User Profile
        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_friends_list_to_user_profile)
        }

        // Navigate to FeedFragment
        feedButton.setOnClickListener {
            findNavController().navigate(R.id.action_friends_list_to_navigation_feed_fragment)
        }

        return view
    }

    private fun loadFriends() {
        val currentUserId = auth.currentUser?.uid ?: return
        val friendsMap = mutableMapOf<String, Friend>()

        database.child("Users").child(currentUserId).child("friends")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (friendSnapshot in snapshot.children) {
                        val friendId = friendSnapshot.key ?: continue
                        database.child("Users").child(friendId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(friendData: DataSnapshot) {
                                val username = friendData.child("Username").value.toString()
                                val profilePictureUrl = friendData.child("profilePictureUrl").value.toString()
                                friendsMap[friendId] = Friend(friendId, username, profilePictureUrl)
                                if (friendsMap.size == snapshot.childrenCount.toInt()) {
                                    friendsAdapter.submitList(friendsMap.values.toList())
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(requireContext(), "Failed to load friend data", Toast.LENGTH_SHORT).show()
                            }
                        })
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
                        addFriendInput.text.clear() // Clear input field
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

    private fun navigateToFriendProfile(friendId: String) {
        val bundle = Bundle().apply {
            putString("friendId", friendId)
        }
        // Ensure navigation to the friend profile fragment is set up in the nav graph
        findNavController().navigate(R.id.navigation_friend_profile, bundle)
    }
}

data class Friend(val id: String, val username: String, val profilePictureUrl: String)
