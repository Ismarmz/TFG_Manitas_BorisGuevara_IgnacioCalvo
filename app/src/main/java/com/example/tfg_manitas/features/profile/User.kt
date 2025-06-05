package com.example.tfg_manitas.features.profile

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val createdAt: String = "",
    val rating: Double = 0.0
)
