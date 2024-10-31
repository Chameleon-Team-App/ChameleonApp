package com.example.mainchameleon.ui.journal

data class JournalEntry(
    var id: String? = null,
    var title: String = "",
    var text: String = "",
    var imageUrl: String? = null // URL of the image in Firebase Storage
)
