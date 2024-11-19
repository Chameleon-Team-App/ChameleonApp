// JournalEntry.kt
package com.example.mainchameleon.ui.journal

import android.graphics.Color

data class JournalEntry(
    val id: String = "",
    val title: String = "",
    val text: String = "",
    val mood: String? = null,
    var imageUrl: String? = null, // URL for uploaded image
    var userId: String = "",
    val backgroundColor: Int = generateRandomColor(),
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
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
            return COLORS[random.nextInt(COLORS.size)]
        }
    }
}
