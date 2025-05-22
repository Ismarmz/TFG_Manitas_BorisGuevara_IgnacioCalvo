package com.example.tfg_manitas.features.chat

data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "sent",
    val type: String = "text" // text o image
)

