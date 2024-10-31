package com.example.mainchameleon.ui.journal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.databinding.FragmentJournalEntryBinding
import com.squareup.picasso.Picasso

class JournalAdapter : RecyclerView.Adapter<JournalAdapter.JournalViewHolder>() {

    private var journalEntries: List<JournalEntry> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val binding = FragmentJournalEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JournalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val journalEntry = journalEntries[position]
        holder.bind(journalEntry)
    }

    override fun getItemCount(): Int = journalEntries.size

    fun submitList(entries: List<JournalEntry>) {
        journalEntries = entries
        notifyDataSetChanged()
    }

    class JournalViewHolder(private val binding: FragmentJournalEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(journalEntry: JournalEntry) {
            binding.titleTextView.text = journalEntry.title
            binding.entryTextView.text = journalEntry.text

            // If there's an image, load it, otherwise hide the ImageView
            if (!journalEntry.imageUrl.isNullOrEmpty()) {
                Picasso.get().load(journalEntry.imageUrl).into(binding.imageView)
                binding.imageView.visibility = View.VISIBLE
            } else {
                binding.imageView.visibility = View.GONE
            }
        }
    }
}
