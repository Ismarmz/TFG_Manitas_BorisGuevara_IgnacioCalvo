package com.example.tfg_manitas.data.repository

import com.example.tfg_manitas.features.profile.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val users = db.collection("users")

    suspend fun createUser(user: User): Result<Unit> {
        return try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(uid: String): Result<User> {
        return try {
            val doc = users.document(uid).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null) Result.success(user)
            else Result.failure(Exception("Usuario no encontrado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
