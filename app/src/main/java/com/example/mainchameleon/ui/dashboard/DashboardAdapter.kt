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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_dashboard, parent, false)
        return DashboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        val entry = entries[position]
        holder.bind(entry)
    }

    override fun getItemCount(): Int = entries.size

    inner class DashboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recentJournalCard: androidx.cardview.widget.CardView =
            itemView.findViewById(R.id.recentJournalCard)
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val recentJournalUsername: TextView = itemView.findViewById(R.id.recentJournalUsername)
        private val recentJournalMood: TextView = itemView.findViewById(R.id.recentJournalMood)
        private val recentJournalTitle: TextView = itemView.findViewById(R.id.recentJournalTitle)
        private val recentJournalText: TextView = itemView.findViewById(R.id.recentJournalText)
        private val recentJournalImage: ImageView = itemView.findViewById(R.id.recentJournalImage)
        private val recentJournalDate: TextView = itemView.findViewById(R.id.recentJournalDate)

        fun bind(entry: JournalEntry) {
            // Title and text
            recentJournalTitle.text = entry.title
            recentJournalText.text = entry.text

            // Mood emoji
            if (!entry.mood.isNullOrEmpty()) {
                recentJournalMood.visibility = View.VISIBLE
                recentJournalMood.text = entry.mood
            } else {
                recentJournalMood.visibility = View.GONE
            }

            // Background color
            try {
                recentJournalCard.setCardBackgroundColor(entry.backgroundColor)
            } catch (e: Exception) {
                recentJournalCard.setCardBackgroundColor(
                    itemView.context.getColor(R.color.default_background)
                )
            }

            // Date formatting
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            recentJournalDate.text = try {
                val date = Date(entry.timestamp)
                "Date: ${dateFormat.format(date)}"
            } catch (e: Exception) {
                "Date: Unknown"
            }

            // Image handling
            if (!entry.imageUrl.isNullOrEmpty()) {
                recentJournalImage.visibility = View.VISIBLE
                Picasso.get().load(entry.imageUrl).into(recentJournalImage)
            } else {
                recentJournalImage.visibility = View.GONE
            }

            // Load user data (profile picture and username)
            loadUserData(entry.userId)
        }

        private fun loadUserData(userId: String) {
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            userRef.child("Username").get().addOnSuccessListener { dataSnapshot ->
                val username = dataSnapshot.getValue(String::class.java)
                recentJournalUsername.text = username ?: "Unknown User"
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
