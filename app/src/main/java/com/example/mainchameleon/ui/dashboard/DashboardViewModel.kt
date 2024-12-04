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

    // LiveData to hold all journal entries
    private val _allEntries = MutableLiveData<List<JournalEntry>>()
    val allEntries: LiveData<List<JournalEntry>> = _allEntries

    // LiveData for streak
    private val _streak = MutableLiveData<Streak>()
    val streak: LiveData<Streak> = _streak

    // LiveData for most recent journal entry
    private val _mostRecentJournal = MutableLiveData<JournalEntry>()
    private val mostRecentJournal: LiveData<JournalEntry> = _mostRecentJournal

    init {
        loadAllEntries()
        loadStreak()
        loadMostRecentJournal()
    }

    // Fetch all journal entries from the database
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
                combinedEntries.sortByDescending { it.timestamp }
                _allEntries.value = combinedEntries
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DashboardViewModel", "Failed to fetch all entries", error.toException())
            }
        })
    }

    // Fetch the most recent journal entry for the current user
    private fun loadMostRecentJournal() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Users/$userId/journals")
        ref.orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val journal = data.getValue(JournalEntry::class.java)
                    journal?.let {
                        it.userId = userId
                        _mostRecentJournal.postValue(it)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DashboardViewModel", "Failed to fetch most recent journal", error.toException())
            }
        })
    }

    fun getMostRecentJournal(): LiveData<JournalEntry> {
        return mostRecentJournal
    }

    // Load the streak data for the current user
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

    // Update the user's streak based on journal entries
    fun updateStreak() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val streakRef = FirebaseDatabase.getInstance().getReference("Users/$userId/streak")
        val journalRef = FirebaseDatabase.getInstance().getReference("Users/$userId/journals")

        journalRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentDate = getCurrentDate()
                var streakData = _streak.value ?: Streak()
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

                if (streakData.lastStreakDate == currentDate) {
                    return
                } else if (hasEntryToday) {
                    streakData.currentStreak += 1
                    streakData.lastStreakDate = currentDate
                } else {
                    streakData.currentStreak = 0
                    streakData.lastStreakDate = currentDate
                }

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
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
    }

    // Get reference to a specific user's data
    fun getUserReference(userId: String): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference("Users").child(userId)
    }
}
