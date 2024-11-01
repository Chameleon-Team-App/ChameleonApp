package com.example.mainchameleon.ui.journal

data class JournalEntry(
    val id: String = "",
    val title: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    var userId: String = "", // Ensure userId is a part of the data structure
    val backgroundColor: Int = generateRandomColor()
) {
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


