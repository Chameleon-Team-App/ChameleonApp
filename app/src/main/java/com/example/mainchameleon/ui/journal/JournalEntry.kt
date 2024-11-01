package com.example.mainchameleon.ui.journal

data class JournalEntry(
    val id: String = "",
    val title: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val userId: String? = null,
    var backgroundColor: Int = -1 // Default -1, set it only if not provided
) {
    init {
        if (backgroundColor == -1) {
            backgroundColor = generateRandomColor() // Generate color only if it hasn’t been set
        }
    }

    companion object {
        fun generateRandomColor(): Int {
            val random = java.util.Random()
            return android.graphics.Color.argb(
                255,
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
            )
        }
    }
}


