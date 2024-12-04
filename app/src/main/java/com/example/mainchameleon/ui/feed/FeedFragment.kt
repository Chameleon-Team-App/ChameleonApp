package com.example.mainchameleon.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R
import com.example.mainchameleon.ui.journal.JournalAdapter
import com.example.mainchameleon.ui.journal.JournalEntry
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FeedFragment : Fragment() {

    private lateinit var feedRecyclerView: RecyclerView
    private lateinit var feedAdapter: JournalAdapter
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feed, container, false)

        // Initialize RecyclerView
        feedRecyclerView = view.findViewById(R.id.feed_recycler_view)
        feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        feedAdapter = JournalAdapter()
        feedRecyclerView.adapter = feedAdapter

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference.child("Users")

        // Load feed data
        loadFeedData()

        return view
    }

    private fun loadFeedData() {
        database.addValueEventListener(object : ValueEventListener {
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
                feedAdapter.submitList(journalEntries)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database errors here
            }
        })
    }
}
