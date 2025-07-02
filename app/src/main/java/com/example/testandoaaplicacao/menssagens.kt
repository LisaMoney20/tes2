package com.example.testandoaaplicacao

data class ChatMessage(
    val text: String,
    val isSentByMe: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
