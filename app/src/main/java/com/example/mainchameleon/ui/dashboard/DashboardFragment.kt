package com.example.mainchameleon.ui.dashboard

import JournalViewModel
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

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var journalViewModel: JournalViewModel
    private lateinit var journalAdapter: JournalAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        // Initialize the ViewModel
        journalViewModel = ViewModelProvider(this).get(JournalViewModel::class.java)

        // Initialize RecyclerView adapter
        journalAdapter = JournalAdapter()
        binding.recyclerView.adapter = journalAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observe the journalEntries LiveData from the ViewModel
        journalViewModel.journalEntries.observe(viewLifecycleOwner, { entries ->
            journalAdapter.submitList(entries) // Submit list to adapter
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
