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

            return if (user != null) {
                // Cargar reviews y calcular promedio
                val reviewsSnapshot = FirebaseFirestore.getInstance()
                    .collection("reviews")
                    .whereEqualTo("reviewedUserId", uid)
                    .get()
                    .await()

                val ratings = reviewsSnapshot.documents.mapNotNull { it.getDouble("rating") }
                val averageRating = if (ratings.isNotEmpty()) ratings.average() else 0.0

                val enrichedUser = user.copy(rating = averageRating)
                Result.success(enrichedUser)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun updateUser(user: User): Result<Unit> {
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

}
