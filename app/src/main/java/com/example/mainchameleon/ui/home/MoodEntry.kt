package com.example.mainchameleon.ui.mood

data class MoodEntry(
    val id: String = "",
    val mood: String = "",
    val sentence: String = "",
    val backgroundColor: Int = generateRandomColor(),
    val timestamp: Long = System.currentTimeMillis(),
    var userId: String = ""
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
