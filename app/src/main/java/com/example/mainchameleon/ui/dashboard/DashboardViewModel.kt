package com.example.mainchameleon.ui.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mainchameleon.ui.journal.JournalEntry
import com.example.mainchameleon.ui.mood.MoodEntry
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

sealed class DashboardEntry {
    data class Journal(val journalEntry: JournalEntry) : DashboardEntry()
    data class Mood(val moodEntry: MoodEntry) : DashboardEntry()
}

class DashboardViewModel : ViewModel() {

    // LiveData to hold all dashboard entries (journals and moods from all users)
    private val _allEntries = MutableLiveData<List<DashboardEntry>>()
    val allEntries: LiveData<List<DashboardEntry>> = _allEntries

    // LiveData for streak
    private val _streak = MutableLiveData<Streak>()
    val streak: LiveData<Streak> = _streak

    init {
        loadAllEntries()
        loadStreak()
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

    /**
     * Loads the current streak data for the user.
     */
    private fun loadStreak() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("DashboardViewModel", "User not logged in")
            return
        }

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

    /**
     * Updates the streak when both Journal and Mood entries are completed for a day.
     */
    fun updateStreakIfNeeded() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("DashboardViewModel", "User not logged in")
            return
        }

        val todayDate = getCurrentDateString()
        val streakRef = FirebaseDatabase.getInstance().getReference("Users/$userId/streak")

        streakRef.get().addOnSuccessListener { snapshot ->
            val currentStreak = snapshot.child("currentStreak").getValue(Int::class.java) ?: 0
            val lastStreakDate = snapshot.child("lastStreakDate").getValue(String::class.java) ?: ""

            if (isSameDay(lastStreakDate, todayDate)) {
                // Streak already updated today
                Log.d("DashboardViewModel", "Streak already updated today")
                return@addOnSuccessListener
            }

            // Check if both Journal and Mood entries are present for today
            val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$userId")
            val journalsRef = databaseRef.child("journals")
            val moodsRef = databaseRef.child("moods")

            val todayTimestamp = getStartOfTodayTimestamp()

            // Query for Journal entries today
            journalsRef.orderByChild("timestamp").startAt(todayTimestamp.toDouble()).endAt((todayTimestamp + 86400000).toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(journalSnapshot: DataSnapshot) {
                        val journalsToday = journalSnapshot.childrenCount > 0

                        // Query for Mood entries today
                        moodsRef.orderByChild("timestamp").startAt(todayTimestamp.toDouble()).endAt((todayTimestamp + 86400000).toDouble())
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(moodSnapshot: DataSnapshot) {
                                    val moodsToday = moodSnapshot.childrenCount > 0

                                    if (journalsToday && moodsToday) {
                                        val newStreak = if (isYesterday(lastStreakDate, todayDate)) {
                                            currentStreak + 1
                                        } else {
                                            1
                                        }

                                        // Update streak in database
                                        streakRef.child("currentStreak").setValue(newStreak)
                                        streakRef.child("lastStreakDate").setValue(todayDate)
                                            .addOnSuccessListener {
                                                _streak.value = Streak(newStreak, todayDate)
                                                Log.d("DashboardViewModel", "Streak updated to $newStreak")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("DashboardViewModel", "Failed to update streak", e)
                                            }
                                    } else {
                                        Log.d("DashboardViewModel", "Both entries not completed today")
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("DashboardViewModel", "Failed to fetch moods for streak", error.toException())
                                }
                            })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("DashboardViewModel", "Failed to fetch journals for streak", error.toException())
                    }
                })
        }.addOnFailureListener { e ->
            Log.e("DashboardViewModel", "Failed to fetch streak data", e)
        }
    }

    /**
     * Utility function to get current date as a string in "yyyy-MM-dd" format.
     */
    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Utility function to check if two dates are the same day.
     */
    private fun isSameDay(lastDate: String, currentDate: String): Boolean {
        return lastDate == currentDate
    }

    /**
     * Utility function to check if last streak date was yesterday.
     */
    private fun isYesterday(lastDate: String, currentDate: String): Boolean {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val last = sdf.parse(lastDate)
            val current = sdf.parse(currentDate)

            if (last == null || current == null) return false

            val calendar = Calendar.getInstance()
            calendar.time = last
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val expectedYesterday = sdf.format(calendar.time)

            return expectedYesterday == currentDate
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Utility function to get the start timestamp of today.
     */
    private fun getStartOfTodayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
