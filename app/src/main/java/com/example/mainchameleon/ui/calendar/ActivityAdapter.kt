package com.example.mainchameleon.ui.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R

class ActivityAdapter(
    private var activities: MutableList<Pair<String, Boolean>>,
    private val onActivityCompleted: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val activityName: TextView = itemView.findViewById(R.id.activityNameTextView)
        val completedCheckBox: CheckBox = itemView.findViewById(R.id.completedCheckBox)
        val deleteButton: View = itemView.findViewById(R.id.deleteButton) // Add this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_tile, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val (activityName, isCompleted) = activities[position]

        // Set the activity name and checkbox state
        holder.activityName.text = activityName
        holder.completedCheckBox.isChecked = isCompleted

        // Handle completed state: Disable interactions and update visuals
        if (isCompleted) {
            holder.activityName.alpha = 0.5f // Dim text
            holder.completedCheckBox.isEnabled = false // Disable checkbox
            holder.deleteButton.isEnabled = false // Disable delete button
            holder.deleteButton.alpha = 0.5f // Dim delete button
        } else {
            holder.activityName.alpha = 1f // Normal text
            holder.completedCheckBox.isEnabled = true // Enable checkbox
            holder.deleteButton.isEnabled = true // Enable delete button
            holder.deleteButton.alpha = 1f // Normal delete button
        }

        // Handle checkbox clicks to toggle completion
        holder.completedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isCompleted) { // Prevent redundant calls for already completed items
                activities[position] = activityName to true // Mark as completed
                onActivityCompleted(position) // Trigger callback for save and refresh
            }
        }

        // Handle delete button click
        holder.deleteButton.setOnClickListener {
            if (!isCompleted) { // Prevent deleting completed activities
                onDeleteClick(position)
            }
        }
    }

    override fun getItemCount(): Int = activities.size

    // Method to update the dataset
    fun updateActivities(newActivities: MutableList<Pair<String, Boolean>>) {
        activities.clear()
        activities.addAll(newActivities)
        notifyDataSetChanged() // Notify the RecyclerView about data changes
    }
}
