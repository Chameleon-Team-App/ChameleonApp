package com.example.mainchameleon.ui.journal

import android.graphics.Color

data class JournalEntry(
    val id: String = "",
    val title: String = "",
    val text: String = "",
    val mood: String? = null, // New field for storing mood emoji
    var imageUrl: String? = null, // URL for uploaded image
    var userId: String = "",
    val backgroundColor: Int = generateRandomColor(), // Randomly generated background color
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        // Define a list of 7 colors that look good against black text
        private val COLORS = listOf(
            Color.parseColor("#FFE4B5"), // Moccasin
            Color.parseColor("#FFDAB9"), // Peach Puff
            Color.parseColor("#FFDEAD"), // Navajo White
            Color.parseColor("#FFCCCB"), // Light Pink
            Color.parseColor("#D8BFD8"), // Thistle
            Color.parseColor("#E6E6FA"), // Lavender
            Color.parseColor("#B0E0E6")  // Powder Blue
        )

        fun generateRandomColor(): Int {
            val random = java.util.Random()
            return COLORS[random.nextInt(COLORS.size)] // Select a random color from the list
        }
    }
}
