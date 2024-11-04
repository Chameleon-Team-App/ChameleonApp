package com.example.mainchameleon.ui.journal

import android.app.AlertDialog
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
import com.example.mainchameleon.ui.mood.MoodEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class JournalAdapter : ListAdapter<Any, JournalAdapter.JournalViewHolder>(JournalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_journal_entry, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val entry = getItem(position)
        holder.bind(entry)
    }

    inner class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val entryTextView: TextView = itemView.findViewById(R.id.entryTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val moodTextView: TextView = itemView.findViewById(R.id.moodTextView)
        private val createdDateTextView: TextView = itemView.findViewById(R.id.createdDateTextView)

        fun bind(entry: Any) {
            when (entry) {
                is JournalEntry -> {
                    titleTextView.text = entry.title
                    entryTextView.text = entry.text
                    moodTextView.visibility = View.GONE

                    // Set background color
                    cardView.setCardBackgroundColor(entry.backgroundColor)

                    // Handle image
                    if (!entry.imageUrl.isNullOrEmpty()) {
                        imageView.visibility = View.VISIBLE
                        Picasso.get().load(entry.imageUrl).into(imageView)
                    } else {
                        imageView.visibility = View.GONE
                    }

                    // Retrieve and display the username and profile picture for the entry's userId
                    val userId = entry.userId
                    loadUserData(userId)

                    // Display creation date
                    if (entry.timestamp != 0L) {
                        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                        createdDateTextView.text = "Date: ${sdf.format(Date(entry.timestamp))}"
                    } else {
                        createdDateTextView.text = "Date: Unknown"
                    }

                    // Set up click listener for deletion
                    cardView.setOnLongClickListener {
                        promptDeleteEntry(entry)
                        true
                    }
                }

                is MoodEntry -> {
                    titleTextView.text = "Mood: ${entry.mood}"
                    entryTextView.text = entry.sentence
                    moodTextView.visibility = View.GONE

                    // Set background color
                    cardView.setCardBackgroundColor(entry.backgroundColor)

                    // No image for moods by default; adjust if necessary
                    imageView.visibility = View.GONE

                    // Retrieve and display the username and profile picture for the entry's userId
                    val userId = entry.userId
                    loadUserData(userId)

                    // Display creation date
                    if (entry.timestamp != 0L) {
                        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                        createdDateTextView.text = "Date: ${sdf.format(Date(entry.timestamp))}"
                    } else {
                        createdDateTextView.text = "Date: Unknown"
                    }

                    // Set up click listener for deletion
                    cardView.setOnLongClickListener {
                        promptDeleteEntry(entry)
                        true
                    }
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

        private fun promptDeleteEntry(entry: Any) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (entry is JournalEntry && entry.userId == userId || entry is MoodEntry && entry.userId == userId) {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Delete Entry")
                    .setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton("Yes") { _, _ ->
                        deleteEntry(entry)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

        private fun deleteEntry(entry: Any) {
            val database = FirebaseDatabase.getInstance().reference

            when (entry) {
                is JournalEntry -> {
                    database.child("Users").child(entry.userId).child("journals").child(entry.id).removeValue()
                }
                is MoodEntry -> {
                    database.child("Users").child(entry.userId).child("moods").child(entry.id).removeValue()
                }
            }
        }
    }

    class JournalDiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is JournalEntry && newItem is JournalEntry -> oldItem.id == newItem.id
                oldItem is MoodEntry && newItem is MoodEntry -> oldItem.id == newItem.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }
    }
}
