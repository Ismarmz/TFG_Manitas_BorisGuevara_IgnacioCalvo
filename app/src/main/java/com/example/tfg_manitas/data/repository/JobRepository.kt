package com.example.tfg_manitas.data.repository

import com.example.tfg_manitas.features.jobs.Job
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class JobRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun listenToAllJobs() = callbackFlow<List<Job>> {
        val listener = db.collection("jobs")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    close(error ?: Exception("Error de conexión"))
                    return@addSnapshotListener
                }
                val jobs = snapshot.map { doc ->
                    doc.toObject(Job::class.java).copy(id = doc.id)
                }
                trySend(jobs)
            }
        awaitClose { listener.remove() }
    }

    fun listenToUserJobs() = callbackFlow<List<Job>> {
        val uid = auth.currentUser?.uid ?: return@callbackFlow
        val listener = db.collection("jobs")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    close(error ?: Exception("Error de conexión"))
                    return@addSnapshotListener
                }
                val jobs = snapshot.map { doc ->
                    doc.toObject(Job::class.java).copy(id = doc.id)
                }
                trySend(jobs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun postJob(job: Job): Result<Unit> {
        return try {
            db.collection("jobs")
                .add(job)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateJob(jobId: String, updatedJob: Job): Result<Unit> {
        return try {
            db.collection("jobs").document(jobId)
                .set(updatedJob)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteJob(jobId: String): Result<Unit> {
        return try {
            db.collection("jobs").document(jobId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
