package com.example.mainchameleon.ui.journal

data class JournalEntry(
    var id: String? = null,
    var title: String = "",
    var text: String = "",
    var imageUrl: String? = null // This stores the image URL in Firebase Storage
)
