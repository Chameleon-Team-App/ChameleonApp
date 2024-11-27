package com.example.mainchameleon.ui.calendar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R
import java.text.SimpleDateFormat
import java.util.*

class WeeklyCalendarAdapter(
    private val context: Context,
    private val currentDate: Date,
    private val onItemClick: (Date) -> Unit
) : RecyclerView.Adapter<WeeklyCalendarAdapter.DayViewHolder>() {

    private val weekDays: List<Date> = generateWeekDates()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val date = weekDays[position]
        holder.dayTextView.text = SimpleDateFormat("EEE", Locale.getDefault()).format(date)

        // Highlight current date
        holder.dayTextView.setBackgroundColor(
            if (SimpleDateFormat("yyyyMMdd").format(date) == SimpleDateFormat("yyyyMMdd").format(currentDate)) {
                ContextCompat.getColor(context, R.color.highlight)
            } else {
                ContextCompat.getColor(context, android.R.color.transparent)
            }
        )

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(date)
        }
    }

    override fun getItemCount(): Int = weekDays.size

    private fun generateWeekDates(): List<Date> {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

        return List(7) {
            val date = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            date
        }
    }

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTextView: TextView = itemView.findViewById(R.id.dayText)
    }
}