package com.example.mainchameleon.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R
import com.example.mainchameleon.ui.journal.JournalAdapter
import com.example.mainchameleon.ui.journal.JournalEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FeedFragment : Fragment() {

    private lateinit var feedRecyclerView: RecyclerView
    private lateinit var feedAdapter: JournalAdapter
    private lateinit var globalFeedButton: Button
    private lateinit var friendsFeedButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var isFriendsFeed = false
    private lateinit var backButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feed, container, false)

        // Initialize Firebase and UI elements
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        feedRecyclerView = view.findViewById(R.id.feed_recycler_view)
        globalFeedButton = view.findViewById(R.id.global_feed_button)
        friendsFeedButton = view.findViewById(R.id.friends_feed_button)
        backButton = view.findViewById(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed() // Handles back navigation
        }

        // Set up RecyclerView
        feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        feedAdapter = JournalAdapter()
        feedRecyclerView.adapter = feedAdapter

        // Load global feed by default
        loadGlobalFeed()

        // Handle feed toggles
        globalFeedButton.setOnClickListener {
            isFriendsFeed = false
            loadGlobalFeed(true) // Pass true to force scroll to top
        }

        friendsFeedButton.setOnClickListener {
            isFriendsFeed = true
            loadFriendsFeed(true) // Pass true to force scroll to top
        }

        return view
    }

    private fun loadGlobalFeed(scrollToTop: Boolean = false) {
        database.child("Users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val journalEntries = mutableListOf<JournalEntry>()
                for (userSnapshot in snapshot.children) {
                    val journals = userSnapshot.child("journals").children
                    for (journal in journals) {
                        val entry = journal.getValue(JournalEntry::class.java)
                        if (entry != null) {
                            journalEntries.add(entry)
                        }
                    }
                }
                journalEntries.sortByDescending { it.timestamp }
                feedAdapter.submitList(journalEntries) {
                    if (scrollToTop) scrollToTop()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database errors
            }
        })
    }

    private fun loadFriendsFeed(scrollToTop: Boolean = false) {
        val currentUserId = auth.currentUser?.uid ?: return
        val friendsRef = database.child("Users").child(currentUserId).child("friends")

        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friendIds = snapshot.children.mapNotNull { it.key }
                val journalEntries = mutableListOf<JournalEntry>()

                for (friendId in friendIds) {
                    database.child("Users").child(friendId).child("journals")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(friendSnapshot: DataSnapshot) {
                                for (journal in friendSnapshot.children) {
                                    val entry = journal.getValue(JournalEntry::class.java)
                                    if (entry != null) {
                                        journalEntries.add(entry)
                                    }
                                }
                                journalEntries.sortByDescending { it.timestamp }
                                feedAdapter.submitList(journalEntries) {
                                    if (scrollToTop) scrollToTop()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle database errors
                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database errors
            }
        })
    }

    private fun scrollToTop() {
        feedRecyclerView.scrollToPosition(0)
    }
}
