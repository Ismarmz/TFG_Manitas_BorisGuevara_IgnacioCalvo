package com.example.tfg_manitas.features.jobs

data class Job(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val dateTime: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "",

    val applicants: List<String> = listOf(),
    val selectedWorkerId: String? = null
)


