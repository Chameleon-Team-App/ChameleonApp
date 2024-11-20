package com.example.mainchameleon.ui.dashboard

import android.app.AlertDialog
import android.graphics.Color
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class DashboardAdapter : RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder>() {

    private val entries = mutableListOf<JournalEntry>()

    fun submitList(newEntries: List<JournalEntry>) {
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
        private val cardView: androidx.cardview.widget.CardView = itemView.findViewById(R.id.cardView)

        fun bind(entry: JournalEntry) {
            // Title and text
            titleTextView.text = entry.title
            entryTextView.text = entry.text

            // Mood emoji
            if (!entry.mood.isNullOrEmpty()) {
                moodTextView.visibility = View.VISIBLE
                moodTextView.text = entry.mood
            } else {
                moodTextView.visibility = View.GONE
            }

            // Background color
            when (entry.mood) {
                "😊" -> cardView.setCardBackgroundColor(Color.parseColor("#67DB60"))
                "😢" -> cardView.setCardBackgroundColor(Color.parseColor("#B0E0E6"))
                "😡" -> cardView.setCardBackgroundColor(Color.parseColor("#f07f7f"))
                "😟" -> cardView.setCardBackgroundColor(Color.parseColor("#F5A15D"))
                else -> { cardView.setCardBackgroundColor(Color.parseColor("#7A7A7A"))}
            }
            //cardView.setCardBackgroundColor(entry.backgroundColor)

            // Date formatting
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            createdDateTextView.text = "Date: ${dateFormat.format(Date(entry.timestamp))}"

            // Image handling
            if (!entry.imageUrl.isNullOrEmpty()) {
                imageView.visibility = View.VISIBLE
                Picasso.get().load(entry.imageUrl).into(imageView)
            } else {
                imageView.visibility = View.GONE
            }

            // Load user data (profile picture and username)
            loadUserData(entry.userId)

            // Enable deletion on long press
            itemView.setOnLongClickListener {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId == entry.userId) {
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

        private fun showDeleteConfirmationDialog(entry: JournalEntry) {
            AlertDialog.Builder(itemView.context)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Yes") { _, _ -> deleteEntry(entry) }
                .setNegativeButton("No", null)
                .show()
        }

        private fun deleteEntry(entry: JournalEntry) {
            val entryRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(entry.userId)
                .child("journals")
                .child(entry.id)

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

    class DashboardDiffCallback : DiffUtil.ItemCallback<JournalEntry>() {
        override fun areItemsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean {
            return oldItem == newItem
        }
    }
}
