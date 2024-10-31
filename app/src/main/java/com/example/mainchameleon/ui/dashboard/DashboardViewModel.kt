import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mainchameleon.ui.journal.JournalEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DashboardViewModel : ViewModel() {

    private val _journalEntries = MutableLiveData<List<JournalEntry>>()
    val journalEntries: LiveData<List<JournalEntry>> = _journalEntries

    init {
        loadJournalEntries()
    }

    private fun loadJournalEntries() {
        val UserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$UserId/journals")

        databaseRef.get().addOnSuccessListener { dataSnapshot ->
            val entries = mutableListOf<JournalEntry>()
            for (entrySnapshot in dataSnapshot.children) {
                val journalEntry = entrySnapshot.getValue(JournalEntry::class.java)
                journalEntry?.let { entries.add(it) }
            }
            _journalEntries.value = entries
        }.addOnFailureListener {
            Log.e("DashboardViewModel", "Failed to fetch journal entries", it)
        }
    }
}
