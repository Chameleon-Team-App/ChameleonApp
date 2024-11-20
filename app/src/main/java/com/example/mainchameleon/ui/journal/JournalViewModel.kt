package com.example.mainchameleon.ui.journal

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class JournalViewModel : ViewModel() {

    private val _currentUserJournalEntries = MutableLiveData<List<JournalEntry>>()
    val currentUserJournalEntries: LiveData<List<JournalEntry>> get() = _currentUserJournalEntries

    init {
        loadCurrentUserJournals()
    }

    private fun loadCurrentUserJournals() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("JournalViewModel", "User not logged in")
            return
        }
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$userId/journals")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val journals = mutableListOf<JournalEntry>()
                for (entrySnapshot in snapshot.children) {
                    val entry = entrySnapshot.getValue(JournalEntry::class.java)
                    entry?.let {
                        it.userId = userId
                        journals.add(it)
                    }
                }
                _currentUserJournalEntries.value = journals.sortedByDescending { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("JournalViewModel", "Failed to load journals.", error.toException())
            }
        })
    }
}
