package com.example.tfg_manitas.data.repository

import com.example.tfg_manitas.features.reviews.Review
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReviewRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val reviewsCol = db.collection("reviews")

    // Publicar una reseña
    suspend fun submitReview(review: Review): Result<Unit> {
        return try {
            val doc = reviewsCol.document()
            doc.set(review.copy(id = doc.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener reseñas para un usuario
    suspend fun getReviewsForUser(userId: String): Result<List<Review>> {
        println("📥 Buscando reseñas para: $userId")  // LOG
        return try {
            val snapshot = reviewsCol
                .whereEqualTo("toUserId", userId)
                .orderBy("timestamp")
                .get()
                .await()
            val list = snapshot.map { it.toObject(Review::class.java) }
            println("✅ Encontradas ${list.size} reseñas")  // LOG
            Result.success(list)
        } catch (e: Exception) {
            println("❌ Error al obtener reseñas: ${e.message}")
            Result.failure(e)
        }
    }


    // Opcional: comprobar si ya existe reseña de este usuario para este job
    suspend fun hasReviewed(jobId: String, fromUserId: String): Result<Boolean> {
        return try {
            val snapshot = reviewsCol
                .whereEqualTo("jobId", jobId)
                .whereEqualTo("fromUserId", fromUserId)
                .get()
                .await()
            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
