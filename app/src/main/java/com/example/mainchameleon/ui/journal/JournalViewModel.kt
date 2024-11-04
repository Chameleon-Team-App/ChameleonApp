package com.example.mainchameleon.ui.journal

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mainchameleon.ui.mood.MoodEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class JournalViewModel : ViewModel() {

    // LiveData for all journal entries (used by JournalFragment)
    private val _currentUserJournalEntries = MutableLiveData<List<JournalEntry>>()
    val currentUserJournalEntries: LiveData<List<JournalEntry>> get() = _currentUserJournalEntries

    // LiveData for all mood entries (used by HomeFragment)
    private val _currentUserMoodEntries = MutableLiveData<List<MoodEntry>>()
    val currentUserMoodEntries: LiveData<List<MoodEntry>> get() = _currentUserMoodEntries

    // LiveData for Dashboard (combining both journals and moods)
    private val _allEntries = MutableLiveData<List<Any>>() // Using Any to hold both types
    val allEntries: LiveData<List<Any>> get() = _allEntries

    init {
        loadCurrentUserJournals()
        loadCurrentUserMoods()
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
                loadAllEntries()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("JournalViewModel", "Failed to load journals.", error.toException())
            }
        })
    }

    private fun loadCurrentUserMoods() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("JournalViewModel", "User not logged in")
            return
        }
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$userId/moods")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val moods = mutableListOf<MoodEntry>()
                for (entrySnapshot in snapshot.children) {
                    val entry = entrySnapshot.getValue(MoodEntry::class.java)
                    entry?.let {
                        it.userId = userId
                        moods.add(it)
                    }
                }
                _currentUserMoodEntries.value = moods.sortedByDescending { it.timestamp }
                loadAllEntries()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("JournalViewModel", "Failed to load moods.", error.toException())
            }
        })
    }

    private fun loadAllEntries() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val journalsRef = FirebaseDatabase.getInstance().getReference("Users/$userId/journals")
        val moodsRef = FirebaseDatabase.getInstance().getReference("Users/$userId/moods")

        journalsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(journalSnapshot: DataSnapshot) {
                val journals = mutableListOf<JournalEntry>()
                for (entrySnapshot in journalSnapshot.children) {
                    val entry = entrySnapshot.getValue(JournalEntry::class.java)
                    entry?.let {
                        it.userId = userId
                        journals.add(it)
                    }
                }

                moodsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(moodSnapshot: DataSnapshot) {
                        val moods = mutableListOf<MoodEntry>()
                        for (entrySnapshot in moodSnapshot.children) {
                            val entry = entrySnapshot.getValue(MoodEntry::class.java)
                            entry?.let {
                                it.userId = userId
                                moods.add(it)
                            }
                        }

                        // Combine journals and moods
                        val combinedEntries: List<Any> = (journals + moods).sortedByDescending {
                            when (it) {
                                is JournalEntry -> it.timestamp
                                is MoodEntry -> it.timestamp
                                else -> 0L
                            }
                        }

                        _allEntries.value = combinedEntries
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("JournalViewModel", "Failed to load moods for Dashboard.", error.toException())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("JournalViewModel", "Failed to load journals for Dashboard.", error.toException())
            }
        })
    }
}
