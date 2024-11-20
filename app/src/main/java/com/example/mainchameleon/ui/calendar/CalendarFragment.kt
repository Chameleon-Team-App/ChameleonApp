package com.example.mainchameleon.ui.calendar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarFragment : Fragment(), OnDayClickListener {

    private lateinit var monthYearText: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var selectedDate: LocalDate
    private lateinit var previousMonthButton: Button
    private lateinit var nextMonthButton: Button
    private lateinit var activitiesRecyclerView: RecyclerView
    private lateinit var activityAdapter: ActivityAdapter
    private var activitiesForSelectedDate = mutableListOf<Pair<String, Boolean>>()
    private var activitiesMap: MutableMap<String, MutableList<Pair<String, Boolean>>> = mutableMapOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_calendar, container, false)

        // Load all activities from SharedPreferences
        loadAllActivities()

        val backButton = rootView.findViewById<MaterialCardView>(R.id.back_button)
        backButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard) // Navigate back to dashboard
        }

        // Initialize UI components
        calendarRecyclerView = rootView.findViewById(R.id.calendarRecyclerView)
        monthYearText = rootView.findViewById(R.id.monthYearTV)
        previousMonthButton = rootView.findViewById(R.id.previousMonthButton)
        nextMonthButton = rootView.findViewById(R.id.nextMonthButton)

        // Set button click listeners for navigation
        previousMonthButton.setOnClickListener { previousMonthAction() }
        nextMonthButton.setOnClickListener { nextMonthAction() }

        // Initialize selected date and set the initial calendar view
        selectedDate = LocalDate.now()
        setMonthView()

        // Initialize Activities RecyclerView
        activitiesRecyclerView = rootView.findViewById(R.id.activitiesRecyclerView)
        activityAdapter = ActivityAdapter(activitiesForSelectedDate) { position ->
            markActivityAsCompleted(position)
        }
        activitiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        activitiesRecyclerView.adapter = activityAdapter

        // Add listener for Add Activity button
        val addActivityButton = rootView.findViewById<Button>(R.id.addActivityButton)
        addActivityButton.setOnClickListener {
            val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
            showAddActivityDialog(formattedDate)
        }

        return rootView
    }

    private fun setMonthView() {
        // Update the month and year text
        monthYearText.text = monthYearFromDate(selectedDate)

        // Generate the days for the selected month
        val daysInMonth = daysInMonthArray(selectedDate)

        // Update the RecyclerView
        calendarRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 7) // 7 columns for the days of the week
            adapter = CalendarAdapter(daysInMonth, this@CalendarFragment)
        }
    }

    private fun daysInMonthArray(date: LocalDate): List<LocalDate?> {
        val daysInMonthArray = mutableListOf<LocalDate?>()
        val yearMonth = YearMonth.from(date)
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstOfMonth = date.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value % 7 // Adjust for Sunday as the start of the week

        // Add empty placeholders for days from the previous month
        for (i in 1..dayOfWeek) {
            daysInMonthArray.add(null) // Use null to represent empty placeholders
        }

        // Add actual days of the current month
        for (day in 1..daysInMonth) {
            daysInMonthArray.add(firstOfMonth.withDayOfMonth(day))
        }

        // Add empty placeholders for days of the next month to fill the grid (6 weeks total)
        while (daysInMonthArray.size < 42) {
            daysInMonthArray.add(null)
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

    override fun onItemClick(position: Int, day: LocalDate?) {
        if (day != null) {
            handleDateSelection(day) // Update selectedDate and reload activities
        } else {
            Toast.makeText(requireContext(), "Invalid day selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddActivityDialog(selectedDate: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_activity, null)
        val activityDropdown = dialogView.findViewById<Spinner>(R.id.activityDropdown)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.mental_health_activities,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            activityDropdown.adapter = adapter
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Activity")
            .setMessage("What activity would you like to add?")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val selectedActivity = activityDropdown.selectedItem.toString()
                if (selectedActivity.isNotBlank()) {
                    activitiesForSelectedDate.add(Pair(selectedActivity, false))
                    activityAdapter.updateActivities(activitiesForSelectedDate)
                    saveActivities(selectedDate)
                } else {
                    Toast.makeText(requireContext(), "No activity selected!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveActivities(date: String) {
        activitiesMap[date] = activitiesForSelectedDate.toMutableList()
        val sharedPreferences = requireContext().getSharedPreferences("activities", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val activitiesJson = Gson().toJson(activitiesMap)
        editor.putString("activities_map", activitiesJson)
        editor.apply()
    }

    private fun markActivityAsCompleted(position: Int) {
        activitiesForSelectedDate[position] = activitiesForSelectedDate[position].copy(second = true)
        activityAdapter.notifyItemChanged(position)

        // Save the updated list
        val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
        saveActivities(formattedDate)
    }

    private fun handleDateSelection(day: LocalDate) {
        selectedDate = day
        val formattedDate = day.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
        loadActivities(formattedDate) // Reload activities for the selected date
    }

    private fun loadActivities(date: String) {
        activitiesForSelectedDate = activitiesMap[date]?.toMutableList() ?: mutableListOf()
        activityAdapter.updateActivities(activitiesForSelectedDate) // Call the new method
    }

    private fun loadAllActivities() {
        val sharedPreferences = requireContext().getSharedPreferences("activities", Context.MODE_PRIVATE)
        val activitiesJson = sharedPreferences.getString("activities_map", "{}")
        val type = object : TypeToken<MutableMap<String, MutableList<Pair<String, Boolean>>>>() {}.type
        activitiesMap = Gson().fromJson(activitiesJson, type) ?: mutableMapOf()
    }
}