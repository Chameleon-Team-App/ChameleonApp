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
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class JournalAdapter :
    ListAdapter<JournalEntry, JournalAdapter.JournalViewHolder>(JournalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_journal_entry, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val entry = getItem(position)
        holder.bind(entry)
        holder.loadUserDetails(entry.userId) // Fetch and set username and profile image
    }

    class JournalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        private val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        private val moodTextView: TextView = view.findViewById(R.id.moodTextView)
        private val entryTextView: TextView = view.findViewById(R.id.entryTextView)
        private val imageView: ImageView = view.findViewById(R.id.imageView)
        private val profileImageView: ImageView = view.findViewById(R.id.profileImageView)
        private val createdDateTextView: TextView = view.findViewById(R.id.createdDateTextView)
        private val cardView: androidx.cardview.widget.CardView = view.findViewById(R.id.cardView)
        private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

        fun bind(entry: JournalEntry) {
            // Set the card background color
            cardView.setCardBackgroundColor(entry.backgroundColor)

            // Set the title and entry text
            titleTextView.text = entry.title
            entryTextView.text = entry.text

            // Display the mood emoji if available
            if (!entry.mood.isNullOrEmpty()) {
                moodTextView.visibility = View.VISIBLE
                moodTextView.text = entry.mood
            } else {
                moodTextView.visibility = View.GONE
            }

            // Load and display the entry image if available
            if (!entry.imageUrl.isNullOrEmpty()) {
                imageView.visibility = View.VISIBLE
                Picasso.get()
                    .load(entry.imageUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(imageView, object : Callback {
                        override fun onSuccess() {}
                        override fun onError(e: Exception?) {
                            imageView.setImageResource(R.drawable.default_profile)
                        }
                    })
            } else {
                imageView.visibility = View.GONE
            }

            // Format and display the created date
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            createdDateTextView.text = "Date: ${dateFormat.format(Date(entry.timestamp))}"
        }

        fun loadUserDetails(userId: String) {
            database.child("Users").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val username = snapshot.child("Username").value?.toString() ?: "Unknown User"
                        val profilePictureUrl = snapshot.child("profilePictureUrl").value?.toString()

                        // Set the username
                        usernameTextView.text = username

                        // Load the profile picture or set a default
                        if (!profilePictureUrl.isNullOrEmpty()) {
                            Picasso.get()
                                .load(profilePictureUrl)
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .into(profileImageView, object : Callback {
                                    override fun onSuccess() {}
                                    override fun onError(e: Exception?) {
                                        profileImageView.setImageResource(R.drawable.default_profile)
                                    }
                                })
                        } else {
                            profileImageView.setImageResource(R.drawable.default_profile)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        usernameTextView.text = "Unknown User"
                        profileImageView.setImageResource(R.drawable.default_profile)
                    }
                })
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
