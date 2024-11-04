package com.example.mainchameleon.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class DashboardAdapter : ListAdapter<DashboardEntry, DashboardAdapter.DashboardViewHolder>(DashboardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_journal_entry, parent, false)
        return DashboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        val entry = getItem(position)
        holder.bind(entry)
    }

    class DashboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val entryTextView: TextView = itemView.findViewById(R.id.entryTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val moodTextView: TextView = itemView.findViewById(R.id.moodTextView)

        fun bind(entry: DashboardEntry) {
            when (entry) {
                is DashboardEntry.Journal -> {
                    val journal = entry.journalEntry
                    titleTextView.text = journal.title
                    entryTextView.text = journal.text
                    moodTextView.visibility = View.VISIBLE
                    moodTextView.text = "Journal"

                    // Set background color
                    cardView.setCardBackgroundColor(journal.backgroundColor)

                    // Handle image
                    if (!journal.imageUrl.isNullOrEmpty()) {
                        imageView.visibility = View.VISIBLE
                        Picasso.get().load(journal.imageUrl).into(imageView)
                    } else {
                        imageView.visibility = View.GONE
                    }

                    // Retrieve and display the username and profile picture for the entry's userId
                    loadUserData(journal.userId)
                }

                is DashboardEntry.Mood -> {
                    val mood = entry.moodEntry
                    titleTextView.text = "Mood: ${mood.mood}"
                    entryTextView.text = mood.sentence
                    moodTextView.visibility = View.VISIBLE
                    moodTextView.text = mood.mood // Display the mood emoji or label

                    // Set background color
                    cardView.setCardBackgroundColor(mood.backgroundColor)

                    // No image for moods by default; adjust if necessary
                    imageView.visibility = View.GONE

                    // Retrieve and display the username and profile picture for the entry's userId
                    loadUserData(mood.userId)
                }
            }
        }

        private fun loadUserData(userId: String) {
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
