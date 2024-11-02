package com.example.mainchameleon.ui.journal

data class JournalEntry(
    val id: String = "",
    val title: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    var userId: String = "",
    val backgroundColor: Int = generateRandomColor(),
    val timestamp: Long = System.currentTimeMillis() // New timestamp field
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



