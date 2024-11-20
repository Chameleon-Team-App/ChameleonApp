package com.example.mainchameleon.ui.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mainchameleon.ui.journal.JournalEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
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

    // Updates streak based on the last entry date
    fun updateStreak() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val streakRef = FirebaseDatabase.getInstance().getReference("Users/$userId/streak")
        val journalRef = FirebaseDatabase.getInstance().getReference("Users/$userId/journals")

        journalRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentDate = getCurrentDate()
                var streakData = _streak.value ?: Streak()

                // Check if there's an entry for today
                var hasEntryToday = false
                for (journalSnapshot in snapshot.children) {
                    val journal = journalSnapshot.getValue(JournalEntry::class.java)
                    if (journal != null) {
                        val entryDate = formatDate(journal.timestamp)
                        if (entryDate == currentDate) {
                            hasEntryToday = true
                            break
                        }
                    }
                }

                // Update streak based on whether there's an entry today
                if (streakData.lastStreakDate == currentDate) {
                    // No update needed, streak already accounted for
                    return
                } else if (hasEntryToday) {
                    streakData.currentStreak += 1
                    streakData.lastStreakDate = currentDate
                } else {
                    streakData.currentStreak = 0
                    streakData.lastStreakDate = currentDate
                }

                // Save updated streak to database
                streakRef.setValue(streakData).addOnCompleteListener {
                    if (it.isSuccessful) {
                        _streak.value = streakData
                    } else {
                        Log.e("DashboardViewModel", "Failed to update streak", it.exception)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DashboardViewModel", "Failed to check journal entries", error.toException())
            }
        })
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
