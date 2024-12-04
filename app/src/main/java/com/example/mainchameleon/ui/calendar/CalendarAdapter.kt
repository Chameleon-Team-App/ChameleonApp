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
    private val hasHeatmapEffect: (LocalDate) -> Boolean // Lambda for heatmap logic
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayText)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    selectedPosition = position
                    notifyDataSetChanged()
                    onDayClickListener.onItemClick(position, days[position])
                }
            }
        }
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

            // Apply heatmap effect if day qualifies
            if (hasHeatmapEffect(day)) {
                holder.itemView.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.glow_effect)
            } else {
                holder.itemView.background = null
            }

            // Highlight current day or selected day
            if (day == LocalDate.now()) {
                holder.dayText.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.highlight_background)
            } else if (position == selectedPosition) {
                holder.dayText.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.selected_background)
            } else {
                holder.dayText.background = null
            }
        } else {
            // Display empty placeholders
            holder.dayText.text = ""
            holder.dayText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.gray))
            holder.itemView.isClickable = false
            holder.itemView.background = null
        }
    }

    override fun getItemCount(): Int = days.size
}