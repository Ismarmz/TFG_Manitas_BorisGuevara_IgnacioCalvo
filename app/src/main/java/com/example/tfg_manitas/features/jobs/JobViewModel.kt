package com.example.tfg_manitas.features.jobs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_manitas.features.profile.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.tfg_manitas.data.repository.ChatRepository

class JobViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userJobs = MutableStateFlow<List<Job>>(emptyList())
    val userJobs: StateFlow<List<Job>> = _userJobs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _availableJobs = MutableStateFlow<List<Job>>(emptyList())
    val availableJobs: StateFlow<List<Job>> = _availableJobs

    private val _userMap = MutableStateFlow<Map<String, User>>(emptyMap())
    val userMap: StateFlow<Map<String, User>> = _userMap

    private val _selectedLocation = MutableStateFlow<Triple<Double, Double, String>?>(null)
    val selectedLocation: StateFlow<Triple<Double, Double, String>?> = _selectedLocation


    fun setSelectedLocation(lat: Double, lng: Double, address: String) {
        _selectedLocation.value = Triple(lat, lng, address)
    }

    init {
        listenToUserJobs()
        listenToAllJobs()
        loadAllUsers()
    }

    fun postJob(
        title: String,
        description: String,
        tags: List<String>,
        dateTime: String,
        paymentAmount: String,
        location: String,
        latitude: Double,
        longitude: Double,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = auth.currentUser ?: return onFailure("Usuario no autenticado")

        val job = Job(
            title = title,
            description = description,
            tags = tags,
            location = location,
            dateTime = dateTime,
            paymentAmount = paymentAmount,
            userId = currentUser.uid,
            latitude = latitude,
            longitude = longitude
        )


        viewModelScope.launch {
            db.collection("jobs")
                .add(job)
                .addOnSuccessListener {
                    Log.d("JobViewModel", "Trabajo subido con éxito: ${it.id}")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("JobViewModel", "Error al subir trabajo", e)
                    onFailure(e.message ?: "Error desconocido")
                }
        }
    }

    fun deleteJobIfAllowed(jobId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("jobs").document(jobId).get().await()
                val job = snapshot.toObject(Job::class.java)

                if (job?.isCompleted == true) {
                    onResult(false, "No se puede eliminar un trabajo ya completado")
                    return@launch
                }

                db.collection("jobs").document(jobId).delete().await()
                listenToUserJobs()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun updateJob(
        jobId: String,
        updatedJob: Job,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("jobs").document(jobId)
            .set(updatedJob)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Error desconocido") }
    }

    suspend fun applyToJob(jobId: String, userId: String): Result<Unit> = runCatching {
        val jobRef = db.collection("jobs").document(jobId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(jobRef)
            val currentApplicants = snapshot.get("applicants") as? List<String> ?: emptyList()
            if (!currentApplicants.contains(userId)) {
                transaction.update(jobRef, "applicants", currentApplicants + userId)
            }
        }.await()
    }

    fun selectWorkerAndCreateChat(
        jobId: String,
        workerId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: return@launch onFailure("Usuario no autenticado")
            try {
                db.collection("jobs").document(jobId).update("selectedWorkerId", workerId).await()
                val chatRepo = ChatRepository()
                chatRepo.getOrCreateChat(jobId, listOf(currentUserId, workerId))
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Error desconocido")
            }
        }
    }

    fun shortlistWorker(
        jobId: String,
        workerId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val jobRef = db.collection("jobs").document(jobId)
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(jobRef)
                    val currentList = snapshot.get("shortlistedWorkerIds") as? List<String> ?: emptyList()
                    if (!currentList.contains(workerId)) {
                        transaction.update(jobRef, "shortlistedWorkerIds", currentList + workerId)
                    }
                }.await()

                val currentUserId = auth.currentUser?.uid
                if (currentUserId != null) {
                    ChatRepository().getOrCreateChat(jobId, listOf(currentUserId, workerId))
                }
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Error al preseleccionar")
            }
        }
    }

    fun listenToUserJobs() {
        val uid = auth.currentUser?.uid ?: return
        _error.value = null
        db.collection("jobs").whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _error.value = error?.message ?: "Error al escuchar trabajos"
                    return@addSnapshotListener
                }
                _userJobs.value = snapshot.map { it.toObject(Job::class.java).copy(id = it.id) }
            }
    }

    fun listenToAllJobs() {
        _error.value = null
        db.collection("jobs").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                _error.value = error?.message ?: "Error al escuchar trabajos"
                return@addSnapshotListener
            }
            _availableJobs.value = snapshot.map { it.toObject(Job::class.java).copy(id = it.id) }
        }
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            try {
                val usersSnapshot = db.collection("users").get().await()
                val reviewsSnapshot = db.collection("reviews").get().await()

                val reviewsByUser = reviewsSnapshot.documents.groupBy {
                    it.getString("toUserId")
                }

                val enrichedUsers = usersSnapshot.documents.mapNotNull { doc ->
                    val user = doc.toObject(User::class.java)
                    val uid = user?.uid
                    if (user != null && uid != null) {
                        val userReviews = reviewsByUser[uid]?.mapNotNull { it.getDouble("rating") } ?: emptyList()
                        val avgRating = if (userReviews.isNotEmpty()) userReviews.average() else 0.0
                        user.copy(rating = avgRating)
                    } else null
                }

                _userMap.value = enrichedUsers.associateBy { it.uid }
            } catch (e: Exception) {
                Log.e("JobViewModel", "Error al cargar usuarios con ratings", e)
            }
        }
    }



    fun refreshJobs() {
        listenToAllJobs()
        listenToUserJobs()
    }
}
