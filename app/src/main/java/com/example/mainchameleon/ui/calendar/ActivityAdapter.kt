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
    private val onActivityCompleted: (Int) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val activityName: TextView = itemView.findViewById(R.id.activityNameTextView)
        val completedCheckBox: CheckBox = itemView.findViewById(R.id.completedCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_tile, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val (activityName, isCompleted) = activities[position]

        holder.activityName.text = activityName
        holder.completedCheckBox.isChecked = isCompleted

        // Disable checkbox if the activity is completed
        holder.completedCheckBox.isEnabled = !isCompleted

        // Handle checkbox state change
        holder.completedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) onActivityCompleted(position)
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
