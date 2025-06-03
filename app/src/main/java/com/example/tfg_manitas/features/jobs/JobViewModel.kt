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


    init {
        listenToUserJobs()
        listenToAllJobs()
        loadAllUsers()
    }

    fun postJob(
        title: String,
        description: String,
        tags: List<String>,
        location: String,
        dateTime: String,
        paymentAmount: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    )
 {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onFailure("Usuario no autenticado")
            return
        }

     val job = Job(
         title = title,
         description = description,
         tags = tags,
         location = location,
         dateTime = dateTime,
         paymentAmount = paymentAmount,
         userId = currentUser.uid
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

    fun loadAllUsers() {
        FirebaseFirestore.getInstance().collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.mapNotNull { it.toObject(User::class.java) }
                _userMap.value = users.associateBy { it.uid }
            }
    }


    fun listenToUserJobs() {
        val uid = auth.currentUser?.uid ?: return
        _error.value = null

        db.collection("jobs")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _error.value = error?.message ?: "Error al escuchar trabajos"
                    return@addSnapshotListener
                }

                val jobsList = snapshot.map { doc ->
                    doc.toObject(Job::class.java).copy(id = doc.id)
                }
                _userJobs.value = jobsList
            }
    }

    fun listenToAllJobs() {
        _error.value = null

        db.collection("jobs")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _error.value = error?.message ?: "Error al escuchar trabajos"
                    return@addSnapshotListener
                }

                val jobsList = snapshot.map { doc ->
                    doc.toObject(Job::class.java).copy(id = doc.id)
                }
                _availableJobs.value = jobsList
            }
    }

    fun deleteJob(jobId: String) {
        _isLoading.value = true
        db.collection("jobs").document(jobId)
            .delete()
            .addOnSuccessListener {
                _isLoading.value = false
                listenToUserJobs()
            }
            .addOnFailureListener {
                _error.value = "Error al eliminar trabajo"
                _isLoading.value = false
            }
    }

    fun getJobById(id: String): StateFlow<Job?> {
        val job = userJobs.value.find { it.id == id }
        return MutableStateFlow(job)
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

    // 🔹 Nueva función: postularse a un trabajo
    suspend fun applyToJob(jobId: String, userId: String): Result<Unit> {
        return try {
            val jobRef = db.collection("jobs").document(jobId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(jobRef)
                val currentApplicants = snapshot.get("applicants") as? List<String> ?: emptyList()

                if (!currentApplicants.contains(userId)) {
                    val updatedApplicants = currentApplicants + userId
                    transaction.update(jobRef, "applicants", updatedApplicants)
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun selectWorkerAndCreateChat(jobId: String, workerId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                onFailure("Usuario no autenticado")
                return@launch
            }

            try {
                db.collection("jobs")
                    .document(jobId)
                    .update("selectedWorkerId", workerId)
                    .await()

                val chatRepo = ChatRepository()
                val result = chatRepo.getOrCreateChat(
                    jobId = jobId,
                    userIds = listOf(currentUserId, workerId)
                )

                if (result.isSuccess) {
                    onSuccess()
                } else {
                    onFailure(result.exceptionOrNull()?.message ?: "Error al crear chat")
                }
            } catch (e: Exception) {
                onFailure(e.message ?: "Error desconocido")
            }
        }
    }

    fun shortlistWorker(jobId: String, workerId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val jobRef = db.collection("jobs").document(jobId)

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(jobRef)
                    val currentList = snapshot.get("shortlistedWorkerIds") as? List<String> ?: emptyList()

                    if (!currentList.contains(workerId)) {
                        val updatedList = currentList + workerId
                        transaction.update(jobRef, "shortlistedWorkerIds", updatedList)
                    }
                }.await()

                // Crear chat al preseleccionar
                val currentUserId = auth.currentUser?.uid
                if (currentUserId != null) {
                    val chatRepo = ChatRepository()
                    chatRepo.getOrCreateChat(jobId, listOf(currentUserId, workerId))
                }

                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Error al preseleccionar")
            }
        }
    }




    fun refreshJobs() {
        listenToAllJobs()
        listenToUserJobs()
    }
}
