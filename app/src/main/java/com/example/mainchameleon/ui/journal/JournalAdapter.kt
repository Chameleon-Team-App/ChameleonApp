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
        holder.itemView.setBackgroundColor(entry.backgroundColor) // Set background color
        holder.bind(entry)
        holder.loadUserDetails(entry.userId) // Fetch and set username and profile image
    }

    class JournalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        private val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        private val moodTextView: TextView = view.findViewById(R.id.moodTextView)
        private val entryTextView: TextView = view.findViewById(R.id.entryTextView)
        private val imageView: ImageView = view.findViewById(R.id.imageView)
        private val profileImageView: ImageView = view.findViewById(R.id.profileImageView) // Add this for profile image
        private val createdDateTextView: TextView = view.findViewById(R.id.createdDateTextView)

        private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

        fun bind(entry: JournalEntry) {
            titleTextView.text = entry.title

            if (!entry.mood.isNullOrEmpty()) {
                moodTextView.text = entry.mood
                moodTextView.visibility = View.VISIBLE
            } else {
                moodTextView.visibility = View.GONE
            }

            entryTextView.text = entry.text

            if (!entry.imageUrl.isNullOrEmpty()) {
                imageView.visibility = View.VISIBLE
                Picasso.get().load(entry.imageUrl).placeholder(R.drawable.default_profile).into(imageView)
            } else {
                imageView.visibility = View.GONE
            }

            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            createdDateTextView.text = "Date: ${dateFormat.format(Date(entry.timestamp))}"
        }

        fun loadUserDetails(userId: String) {
            database.child("Users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("Username").value?.toString() ?: "Unknown"
                    val profilePictureUrl = snapshot.child("profilePictureUrl").value?.toString()

                    usernameTextView.text = username

                    // Load profile image
                    if (!profilePictureUrl.isNullOrEmpty()) {
                        Picasso.get().load(profilePictureUrl).placeholder(R.drawable.default_profile).into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.default_profile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    usernameTextView.text = "Unknown"
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
