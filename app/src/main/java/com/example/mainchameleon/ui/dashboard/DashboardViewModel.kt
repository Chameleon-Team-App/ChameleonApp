package com.example.mainchameleon.ui.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mainchameleon.ui.journal.JournalEntry
import com.google.firebase.database.*

class DashboardViewModel : ViewModel() {

    // LiveData to hold all journal entries from all users
    private val _allEntries = MutableLiveData<List<JournalEntry>>()
    val allEntries: LiveData<List<JournalEntry>> = _allEntries

    // LiveData for streak
    private val _streak = MutableLiveData<Streak>()
    val streak: LiveData<Streak> = _streak

    init {
        loadAllEntries()
        loadStreak()
    }

    // Fetches all journal entries from all users
    fun loadAllEntries() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val combinedEntries = mutableListOf<JournalEntry>()

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    val userJournalsSnapshot = userSnapshot.child("journals")

                    for (journalSnapshot in userJournalsSnapshot.children) {
                        val journalEntry = journalSnapshot.getValue(JournalEntry::class.java)
                        journalEntry?.let {
                            it.userId = userId
                            combinedEntries.add(it)
                        }
                    }
                }

                // Sort entries by timestamp descending
                combinedEntries.sortByDescending { it.timestamp }
                _allEntries.value = combinedEntries
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DashboardViewModel", "Failed to fetch all entries", error.toException())
            }
        })
    }

    // Loads the current streak data for the user
    fun loadStreak() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val streakRef = FirebaseDatabase.getInstance().getReference("Users/$userId/streak")

        streakRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val streakData = snapshot.getValue(Streak::class.java) ?: Streak()
                _streak.value = streakData
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DashboardViewModel", "Failed to fetch streak data", error.toException())
            }
        })
    }
}
