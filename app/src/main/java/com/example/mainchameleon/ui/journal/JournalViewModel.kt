package com.example.mainchameleon.ui.journal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class JournalViewModel : ViewModel() {

    // Store the list of journal entries
    private val _journalEntries = MutableLiveData<MutableList<JournalEntry>>().apply {
        value = mutableListOf() // Initialize with an empty list
    }

    // Expose the list as LiveData so the UI can observe it
    val journalEntries: LiveData<MutableList<JournalEntry>> = _journalEntries

    // Method to add a new journal entry
    fun addJournalEntry(title: String, entry: String) {
        val currentEntries = _journalEntries.value ?: mutableListOf()
        currentEntries.add(JournalEntry(title, entry))
        currentEntries.also { _journalEntries.value = it }
    }

    // Optional: Method to clear all journal entries (if needed)
    fun clearJournalEntries() {
        _journalEntries.value = mutableListOf()
    }
}
