package com.example.tfg_manitas.features.chat

data class Chat(
    val id: String = "",
    val jobId: String = "",
    val userIds: List<String> = listOf(),
    val lastMessage: String = "",
    val lastTimestamp: Long = System.currentTimeMillis(),
)
