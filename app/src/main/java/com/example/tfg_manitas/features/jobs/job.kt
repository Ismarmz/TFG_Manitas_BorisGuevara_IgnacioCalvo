package com.example.tfg_manitas.features.jobs

data class Job(
    val id: String = "", // ← nuevo campo
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val dateTime: String = "",
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

