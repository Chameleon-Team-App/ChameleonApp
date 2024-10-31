package com.example.mainchameleon.ui.journal

data class JournalEntry(
    val id: String? = null, // Nullable ID for Firebase key
    val title: String = "",
    val text: String = "",
    val imageUrl: String? = null // URL of the uploaded image if available
)
