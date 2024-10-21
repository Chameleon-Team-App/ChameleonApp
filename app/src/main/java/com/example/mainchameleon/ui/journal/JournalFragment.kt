package com.example.mainchameleon.ui.journal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mainchameleon.databinding.FragmentJournalBinding

class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!
    private val journalViewModel: JournalViewModel by viewModels()

    private lateinit var journalAdapter: JournalAdapter // Assuming you have a RecyclerView adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        journalAdapter = JournalAdapter() // Initialize your adapter
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = journalAdapter
        }

        // Observe the LiveData for changes
        journalViewModel.journalEntries.observe(viewLifecycleOwner, Observer { entries ->
            journalAdapter.submitList(entries) // Update RecyclerView adapter with new list
        })

        // Handle save button click
        binding.button2.setOnClickListener {
            val title = binding.titleEntryBox.text.toString()
            val entry = binding.journalEntryText.text.toString()
            if (title.isNotEmpty() && entry.isNotEmpty()) {
                journalViewModel.addJournalEntry(title, entry)
                binding.titleEntryBox.text.clear()
                binding.journalEntryText.text.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
