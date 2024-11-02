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
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class JournalAdapter : ListAdapter<JournalEntry, JournalAdapter.JournalViewHolder>(JournalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_journal_entry, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val journalEntry = getItem(position)
        holder.bind(journalEntry)
    }

    class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: View = itemView.findViewById(R.id.cardView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val entryTextView: TextView = itemView.findViewById(R.id.entryTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)

        fun bind(journalEntry: JournalEntry) {
            titleTextView.text = journalEntry.title
            entryTextView.text = journalEntry.text
            cardView.setBackgroundColor(journalEntry.backgroundColor)

            if (journalEntry.imageUrl != null) {
                imageView.visibility = View.VISIBLE
                Picasso.get().load(journalEntry.imageUrl).into(imageView)
            } else {
                imageView.visibility = View.GONE
            }

            // Retrieve and display the username and profile picture for each entry's userId
            val userId = journalEntry.userId
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            // Retrieve username
            userRef.child("Username").get().addOnSuccessListener { dataSnapshot ->
                val username = dataSnapshot.getValue(String::class.java)
                usernameTextView.text = username ?: "Unknown User"
            }.addOnFailureListener {
                usernameTextView.text = "Error Loading User"
            }

            // Retrieve profile picture URL
            userRef.child("profilePictureUrl").get().addOnSuccessListener { dataSnapshot ->
                val profilePictureUrl = dataSnapshot.getValue(String::class.java)
                if (!profilePictureUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(profilePictureUrl)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(profileImageView)
                } else {
                    profileImageView.setImageResource(R.drawable.default_profile)
                }
            }.addOnFailureListener {
                profileImageView.setImageResource(R.drawable.default_profile)
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
