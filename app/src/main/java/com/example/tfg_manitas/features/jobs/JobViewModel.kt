package com.example.tfg_manitas.features.jobs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

    fun postJob(
        title: String,
        description: String,
        category: String,
        location: String,
        dateTime: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onFailure("Usuario no autenticado")
            return
        }

        val job = Job(
            title = title,
            description = description,
            category = category,
            location = location,
            dateTime = dateTime,
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
                listenToUserJobs() // recargar lista
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

    init {
        listenToUserJobs()
        listenToAllJobs()
    }


}
