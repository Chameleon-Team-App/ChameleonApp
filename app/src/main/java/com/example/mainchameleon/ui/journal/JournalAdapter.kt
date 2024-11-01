package com.example.mainchameleon.ui.journal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R
import com.example.mainchameleon.ui.journal.JournalAdapter.JournalViewHolder
import com.example.mainchameleon.ui.journal.JournalEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class JournalAdapter : ListAdapter<JournalEntry, JournalViewHolder>(JournalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_journal_entry, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val journalEntry = getItem(position)
        holder.bind(journalEntry)
    }

    class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: View = itemView.findViewById(R.id.cardView) // CardView container
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val entryTextView: TextView = itemView.findViewById(R.id.entryTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(journalEntry: JournalEntry) {
            titleTextView.text = journalEntry.title
            entryTextView.text = journalEntry.text

            // Set the background color
            cardView.setBackgroundColor(journalEntry.backgroundColor)

            if (journalEntry.imageUrl != null) {
                imageView.visibility = View.VISIBLE
                Picasso.get().load(journalEntry.imageUrl).into(imageView)
            } else {
                imageView.visibility = View.GONE
            }

            // Retrieve the current user ID from FirebaseAuth
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                userRef.child("Username").get().addOnSuccessListener { dataSnapshot ->
                    val username = dataSnapshot.getValue(String::class.java)
                    usernameTextView.text = username ?: "Unknown User"
                }.addOnFailureListener {
                    usernameTextView.text = "Error Loading User"
                }
            } else {
                usernameTextView.text = "No User ID"
            }
        }
    }

    class JournalDiffCallback : DiffUtil.ItemCallback<JournalEntry>() {
        override fun areItemsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean {
            return oldItem == newItem
        }
    }
}
