package com.example.mainchameleon.ui.dashboard

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R
import com.example.mainchameleon.ui.journal.JournalEntry
import com.example.mainchameleon.ui.mood.MoodEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class DashboardAdapter : RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder>() {

    private val entries = mutableListOf<DashboardEntry>()

    fun submitList(newEntries: List<DashboardEntry>) {
        entries.clear()
        entries.addAll(newEntries)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_journal_entry, parent, false)
        return DashboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        val entry = entries[position]
        holder.bind(entry)
    }

    override fun getItemCount(): Int = entries.size

    inner class DashboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val moodTextView: TextView = itemView.findViewById(R.id.moodTextView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val entryTextView: TextView = itemView.findViewById(R.id.entryTextView)
        private val createdDateTextView: TextView = itemView.findViewById(R.id.createdDateTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val cardView: androidx.cardview.widget.CardView = itemView.findViewById(R.id.cardView) // Initialize cardView
        fun bind(entry: DashboardEntry) {
            when (entry) {
                is DashboardEntry.Journal -> {
                    val journal = entry.journalEntry
                    titleTextView.text = journal.title
                    entryTextView.text = journal.text
                    moodTextView.visibility = View.VISIBLE
                    moodTextView.text = "Journal"

                    // Set background color for journal entries
                    cardView.setCardBackgroundColor(journal.backgroundColor)

                    // Display formatted date
                    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(Date(journal.timestamp))
                    createdDateTextView.text = "Date: $formattedDate"

                    if (!journal.imageUrl.isNullOrEmpty()) {
                        imageView.visibility = View.VISIBLE
                        Picasso.get().load(journal.imageUrl).into(imageView)
                    } else {
                        imageView.visibility = View.GONE
                    }

                    loadUserData(journal.userId)
                }

                is DashboardEntry.Mood -> {
                    val mood = entry.moodEntry
                    titleTextView.text = "Mood: ${mood.mood}"
                    entryTextView.text = mood.sentence
                    moodTextView.visibility = View.VISIBLE
                    moodTextView.text = mood.mood

                    // Set background color for mood entries
                    cardView.setCardBackgroundColor(mood.backgroundColor)

                    // Display formatted date
                    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(Date(mood.timestamp))
                    createdDateTextView.text = "Date: $formattedDate"

                    imageView.visibility = View.GONE
                    loadUserData(mood.userId)
                }
            }

            // Enable deletion on long press for entries owned by the current user
            itemView.setOnLongClickListener {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId == (entry as? DashboardEntry.Journal)?.journalEntry?.userId
                    || currentUserId == (entry as? DashboardEntry.Mood)?.moodEntry?.userId) {
                    showDeleteConfirmationDialog(entry)
                }
                true
            }
        }


        private fun loadUserData(userId: String) {
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            userRef.child("Username").get().addOnSuccessListener { dataSnapshot ->
                val username = dataSnapshot.getValue(String::class.java)
                usernameTextView.text = username ?: "Unknown User"
            }

            userRef.child("profilePictureUrl").get().addOnSuccessListener { dataSnapshot ->
                val profilePictureUrl = dataSnapshot.getValue(String::class.java)
                if (!profilePictureUrl.isNullOrEmpty()) {
                    Picasso.get().load(profilePictureUrl).into(profileImageView)
                } else {
                    profileImageView.setImageResource(R.drawable.default_profile)
                }
            }
        }

        private fun showDeleteConfirmationDialog(entry: DashboardEntry) {
            AlertDialog.Builder(itemView.context)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteEntry(entry)
                }
                .setNegativeButton("No", null)
                .show()
        }

        private fun deleteEntry(entry: DashboardEntry) {
            val userId = (entry as? DashboardEntry.Journal)?.journalEntry?.userId
                ?: (entry as? DashboardEntry.Mood)?.moodEntry?.userId
            val entryRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId!!)
                .child(if (entry is DashboardEntry.Journal) "journals" else "moods")
                .child((entry as? DashboardEntry.Journal)?.journalEntry?.id ?: (entry as DashboardEntry.Mood).moodEntry.id)

            entryRef.removeValue().addOnSuccessListener {
                Toast.makeText(itemView.context, "Entry deleted", Toast.LENGTH_SHORT).show()

                // Remove the deleted entry from the list and refresh the adapter
                val position = entries.indexOf(entry)
                if (position != -1) {
                    entries.removeAt(position)
                    notifyItemRemoved(position)
                }
            }.addOnFailureListener {
                Toast.makeText(itemView.context, "Failed to delete entry", Toast.LENGTH_SHORT).show()
            }
        }

    }

    class DashboardDiffCallback : DiffUtil.ItemCallback<DashboardEntry>() {
        override fun areItemsTheSame(oldItem: DashboardEntry, newItem: DashboardEntry): Boolean {
            return when {
                oldItem is DashboardEntry.Journal && newItem is DashboardEntry.Journal ->
                    oldItem.journalEntry.id == newItem.journalEntry.id
                oldItem is DashboardEntry.Mood && newItem is DashboardEntry.Mood ->
                    oldItem.moodEntry.id == newItem.moodEntry.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: DashboardEntry, newItem: DashboardEntry): Boolean {
            return oldItem == newItem
        }
    }
}
