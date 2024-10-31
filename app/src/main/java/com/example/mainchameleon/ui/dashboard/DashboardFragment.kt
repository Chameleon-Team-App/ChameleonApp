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

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var binding: FragmentDashboardBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        // Set up the RecyclerView
        val adapter = JournalAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Observe the journalEntries LiveData from the ViewModel
        dashboardViewModel.journalEntries.observe(viewLifecycleOwner, { entries ->
            adapter.submitList(entries)  // Pass the data to the adapter
        })

        return binding.root
    }
}
