import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mainchameleon.ui.journal.JournalEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JournalViewModel : ViewModel() {

    private val _journalEntries = MutableLiveData<List<JournalEntry>>()
    val journalEntries: LiveData<List<JournalEntry>> get() = _journalEntries

    init {
        loadJournalEntries()
    }

    private fun loadJournalEntries() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries = mutableListOf<JournalEntry>()
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    val journalSnapshot = userSnapshot.child("journals")

                    for (entrySnapshot in journalSnapshot.children) {
                        val entry = entrySnapshot.getValue(JournalEntry::class.java)
                        entry?.let {
                            it.userId = userId // Ensure userId is set for each entry
                            entries.add(it)
                        }
                    }
                }
                _journalEntries.value =
                    entries.sortedByDescending { it.timestamp } // Sort by timestamp
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("JournalViewModel", "Failed to load data.", error.toException())
            }
        })
    }
}