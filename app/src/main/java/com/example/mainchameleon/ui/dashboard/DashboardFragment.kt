package com.example.mainchameleon.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mainchameleon.databinding.FragmentDashboardBinding
import com.example.mainchameleon.ui.journal.JournalAdapter
import com.example.mainchameleon.ui.journal.JournalEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: JournalAdapter
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        // Initialize Firebase database reference for the current user
        database = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser?.uid ?: "").child("journals")

        adapter = JournalAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        fetchJournalEntries()

        return binding.root
    }

    private fun fetchJournalEntries() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val journalEntries = mutableListOf<JournalEntry>()
                for (data in snapshot.children) {
                    val entry = data.getValue(JournalEntry::class.java)
                    if (entry != null) {
                        journalEntries.add(entry)
                    }
                }
                adapter.submitList(journalEntries)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
    }
}
