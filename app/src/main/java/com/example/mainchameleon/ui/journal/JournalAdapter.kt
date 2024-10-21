package com.example.mainchameleon.ui.journal

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R
import kotlin.random.Random
import androidx.cardview.widget.CardView

class JournalAdapter : RecyclerView.Adapter<JournalAdapter.JournalViewHolder>() {

    private var journalList: List<JournalEntry> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_journal_entry, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val journalEntry = journalList[position]
        holder.bind(journalEntry)
    }

    override fun getItemCount(): Int {
        return journalList.size
    }

    fun submitList(list: List<JournalEntry>) {
        journalList = list
        notifyDataSetChanged()
    }

    class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val entryTextView: TextView = itemView.findViewById(R.id.entryTextView)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)

        fun bind(journalEntry: JournalEntry) {
            titleTextView.text = journalEntry.title
            entryTextView.text = journalEntry.entry
            cardView.setBackgroundColor(generateRandomColor())
        }

        private fun generateRandomColor(): Int {
            val random = Random
            return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256))
        }
    }
}
