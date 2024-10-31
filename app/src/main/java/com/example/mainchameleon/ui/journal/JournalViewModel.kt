package com.example.mainchameleon.ui.journal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class JournalViewModel : ViewModel() {

    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("journals")
    private val _journalEntries = MutableLiveData<List<JournalEntry>>()
    val journalEntries: LiveData<List<JournalEntry>> = _journalEntries

    init {
        loadJournalEntries()
    }

    // Create or Update journal entry
    fun saveJournalEntry(journalEntry: JournalEntry) {
        val key = journalEntry.id ?: databaseRef.push().key
        key?.let {
            databaseRef.child(it).setValue(journalEntry).addOnCompleteListener {
                if (it.isSuccessful) {
                    loadJournalEntries()
                }
            }
        }
    }

    // Delete journal entry
    fun deleteJournalEntry(journalId: String) {
        databaseRef.child(journalId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                loadJournalEntries()
            }
        }
    }

    // Load journal entries from Firebase
    private fun loadJournalEntries() {
        databaseRef.get().addOnSuccessListener { dataSnapshot ->
            val entries = mutableListOf<JournalEntry>()
            for (entrySnapshot in dataSnapshot.children) {
                val journalEntry = entrySnapshot.getValue(JournalEntry::class.java)
                journalEntry?.let { entries.add(it) }
            }
            _journalEntries.value = entries
        }
    }
}
