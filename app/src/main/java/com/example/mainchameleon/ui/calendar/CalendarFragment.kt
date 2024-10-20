package com.example.mainchameleon.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R
import com.example.mainchameleon.ui.calendar.CalendarAdapter.OnDayClickListener
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarFragment : Fragment(), OnDayClickListener {
    private lateinit var monthYearText: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var selectedDate: LocalDate
    private lateinit var previousMonthButton: Button
    private lateinit var nextMonthButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_calendar, container, false)

        calendarRecyclerView = rootView.findViewById(R.id.calendarRecyclerView)
        monthYearText = rootView.findViewById(R.id.monthYearTV)
        previousMonthButton = rootView.findViewById(R.id.previousMonthButton)
        nextMonthButton = rootView.findViewById(R.id.nextMonthButton)

        previousMonthButton.setOnClickListener {
            previousMonthAction()
        }

        nextMonthButton.setOnClickListener {
            nextMonthAction()
        }

        selectedDate = LocalDate.now()
        setMonthView()

        return rootView
    }

    private fun setMonthView() {
        monthYearText.text = monthYearFromDate(selectedDate)
        val daysInMonth = daysInMonthArray(selectedDate)

        calendarRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)  // Use requireContext() in fragments
            adapter = CalendarAdapter(daysInMonth, this@CalendarFragment)
        }
    }

    private fun daysInMonthArray(date: LocalDate?): ArrayList<String> {
        val daysInMonthArray = ArrayList<String>()
        val yearMonth = YearMonth.from(date)

        val daysInMonth = yearMonth.lengthOfMonth()
        val firstOfMonth = selectedDate.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value

        for (i in 1..42) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("")
            } else {
                daysInMonthArray.add((i - dayOfWeek).toString())
            }
        }
        return daysInMonthArray
    }

    private fun monthYearFromDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return date.format(formatter)
    }

    private fun previousMonthAction() {
        selectedDate = selectedDate.minusMonths(1)
        setMonthView()
    }

    private fun nextMonthAction() {
        selectedDate = selectedDate.plusMonths(1)
        setMonthView()
    }

    override fun onItemClick(position: Int, dayText: String?) {
        if (!dayText.isNullOrEmpty()) {
            val message = "Selected Date $dayText ${monthYearFromDate(selectedDate)}"
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }
}