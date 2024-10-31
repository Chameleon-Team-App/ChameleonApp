package com.example.mainchameleon.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mainchameleon.ui.journal.JournalEntry

class DashboardViewModel : ViewModel() {

    // LiveData to hold the list of journal entries
    private val _journalEntries = MutableLiveData<List<JournalEntry>>()
    val journalEntries: LiveData<List<JournalEntry>> = _journalEntries

    init {
        // Load journal entries (for now, we will use a placeholder)
        loadJournalEntries()
    }

    // Function to load the journal entries
    private fun loadJournalEntries() {
        // Sample journal entries (you should replace this with data from Firebase or another source)
        val sampleEntries = listOf(
            JournalEntry(title = "First Entry", text = "This is the first journal entry."),
            JournalEntry(title = "Second Entry", text = "This is the second journal entry.")
        )
        _journalEntries.value = sampleEntries
    }
}