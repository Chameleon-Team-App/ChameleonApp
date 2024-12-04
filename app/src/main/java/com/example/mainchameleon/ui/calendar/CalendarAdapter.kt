package com.example.mainchameleon.ui.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R
import java.time.LocalDate

class CalendarAdapter(
    private val days: List<LocalDate?>,
    private val onDayClickListener: OnDayClickListener,
    private val getCompletionScore: (LocalDate) -> Float // Pass completion score logic
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayText)
        val heatmapCircle: View = itemView.findViewById(R.id.heatmapCircle) // Add circle in layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]

        if (day != null) {
            // Display the day number
            holder.dayText.text = day.dayOfMonth.toString()
            holder.dayText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
            holder.itemView.isClickable = true

            // Get completion score for the day
            val completionScore = getCompletionScore(day)
            if (completionScore > 0) {
                // Adjust heatmap circle size and opacity
                holder.heatmapCircle.apply {
                    visibility = View.VISIBLE
                    scaleX = completionScore * 2 // Scale based on completion score
                    scaleY = completionScore * 2
                    alpha = completionScore // Set opacity
                }
            } else {
                holder.heatmapCircle.visibility = View.GONE
            }

            // Handle click events
            holder.itemView.setOnClickListener {
                onDayClickListener.onItemClick(position, day)
            }
        } else {
            holder.dayText.text = ""
            holder.heatmapCircle.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = days.size
}