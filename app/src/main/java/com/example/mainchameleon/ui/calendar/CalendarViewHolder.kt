package com.example.mainchameleon.ui.calendar

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R
import com.example.mainchameleon.ui.calendar.CalendarAdapter.OnDayClickListener

class CalendarViewHolder(itemView: View, private val onItemListener: OnDayClickListener) :
    RecyclerView.ViewHolder(itemView), View.OnClickListener {
    val dayOfMonth: TextView = itemView.findViewById(R.id.cellDayText)

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        onItemListener.onItemClick(bindingAdapterPosition, dayOfMonth.text.toString())
    }
}