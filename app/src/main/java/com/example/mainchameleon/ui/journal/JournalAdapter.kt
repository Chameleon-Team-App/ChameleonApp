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
import com.squareup.picasso.Picasso

class JournalAdapter :
    ListAdapter<JournalEntry, JournalAdapter.JournalViewHolder>(JournalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_journal_entry, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class JournalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        private val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        private val moodTextView: TextView = view.findViewById(R.id.moodTextView)
        private val entryTextView: TextView = view.findViewById(R.id.entryTextView)
        private val imageView: ImageView = view.findViewById(R.id.imageView)
        private val createdDateTextView: TextView = view.findViewById(R.id.createdDateTextView)

        fun bind(entry: JournalEntry) {
            // Set username (replace with actual username retrieval logic if needed)
            usernameTextView.text = entry.userId

            // Set title
            titleTextView.text = entry.title

            // Set mood emoji
            if (!entry.mood.isNullOrEmpty()) {
                moodTextView.text = entry.mood
                moodTextView.visibility = View.VISIBLE
            } else {
                moodTextView.visibility = View.GONE
            }

            // Set journal text
            entryTextView.text = entry.text

            // Set image if present
            if (!entry.imageUrl.isNullOrEmpty()) {
                imageView.visibility = View.VISIBLE
                Picasso.get()
                    .load(entry.imageUrl)
                    .placeholder(R.drawable.default_profile) // Placeholder image
                    .into(imageView)
            } else {
                imageView.visibility = View.GONE
            }

            // Set timestamp
            createdDateTextView.text = "Date: ${entry.timestamp}"
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
