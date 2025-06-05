package com.example.tfg_manitas.features.jobs

import com.google.firebase.firestore.PropertyName

data class Job(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val location: String = "",
    val dateTime: String = "",
    val paymentAmount: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "",
    @get:PropertyName("isCompleted") @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,
    val applicants: List<String> = listOf(),
    val shortlistedWorkerIds: List<String> = listOf(),
    val selectedWorkerId: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0


)




