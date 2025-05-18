package com.example.tfg_manitas.features.reviews

data class Review(
    val id: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val jobId: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
