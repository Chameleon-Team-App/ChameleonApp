package com.example.mainchameleon.ui.dashboard

import DashboardViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mainchameleon.databinding.FragmentDashboardBinding
import com.example.mainchameleon.ui.journal.JournalAdapter

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var journalAdapter: JournalAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        // Initialize the adapter and set it to RecyclerView
        journalAdapter = JournalAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = journalAdapter

        // Observe the journal entries and update the adapter when data changes
        dashboardViewModel.journalEntries.observe(viewLifecycleOwner) { entries ->
            journalAdapter.submitList(entries)
        }

        return binding.root
    }
}
