package com.example.mainchameleon.ui.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mainchameleon.ui.journal.JournalEntry
import com.example.mainchameleon.ui.mood.MoodEntry
import com.google.firebase.database.*

/**
 * Sealed class to represent different types of dashboard entries.
 */
sealed class DashboardEntry {
    data class Journal(val journalEntry: JournalEntry) : DashboardEntry()
    data class Mood(val moodEntry: MoodEntry) : DashboardEntry()
}

class DashboardViewModel : ViewModel() {

    // LiveData to hold all dashboard entries (journals and moods from all users)
    private val _allEntries = MutableLiveData<List<DashboardEntry>>()
    val allEntries: LiveData<List<DashboardEntry>> = _allEntries

    init {
        loadAllEntries()
    }

    /**
     * Fetches all journal and mood entries from all users and combines them.
     */
    private fun loadAllEntries() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val combinedEntries = mutableListOf<DashboardEntry>()

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    val userJournalsSnapshot = userSnapshot.child("journals")
                    val userMoodsSnapshot = userSnapshot.child("moods")

                    // Fetch journals for the user
                    for (journalSnapshot in userJournalsSnapshot.children) {
                        val journalEntry = journalSnapshot.getValue(JournalEntry::class.java)
                        journalEntry?.let {
                            it.userId = userId
                            combinedEntries.add(DashboardEntry.Journal(it))
                        }
                    }

                    // Fetch moods for the user
                    for (moodSnapshot in userMoodsSnapshot.children) {
                        val moodEntry = moodSnapshot.getValue(MoodEntry::class.java)
                        moodEntry?.let {
                            it.userId = userId
                            combinedEntries.add(DashboardEntry.Mood(it))
                        }
                    }
                }

                // Sort combined entries by timestamp descending
                combinedEntries.sortByDescending { entry ->
                    when (entry) {
                        is DashboardEntry.Journal -> entry.journalEntry.timestamp
                        is DashboardEntry.Mood -> entry.moodEntry.timestamp
                    }
                }

                _allEntries.value = combinedEntries
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DashboardViewModel", "Failed to fetch all entries", error.toException())
            }

        })
    }

}
