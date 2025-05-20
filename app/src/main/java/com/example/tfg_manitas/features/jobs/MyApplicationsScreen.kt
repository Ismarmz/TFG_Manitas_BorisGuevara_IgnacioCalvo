package com.example.tfg_manitas.features.jobs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.tfg_manitas.data.repository.ReviewRepository
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MyApplicationsScreen(
    jobViewModel: JobViewModel = viewModel(),
    reviewRepo: ReviewRepository = ReviewRepository(),
    navController: NavHostController
) {
    val allJobs by jobViewModel.availableJobs.collectAsState()
    val isLoading by jobViewModel.isLoading.collectAsState()
    val error by jobViewModel.error.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val context = LocalContext.current

    // Solo mostrar trabajos donde me postulé
    val appliedJobs = allJobs.filter {
        it.applicants.contains(currentUserId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Mis Postulaciones", style = MaterialTheme.typography.h5)
        Spacer(Modifier.height(16.dp))

        when {
            isLoading -> CircularProgressIndicator()
            !error.isNullOrEmpty() -> Text("Error: $error", color = Color.Red)
            appliedJobs.isEmpty() -> Text("Aún no te has postulado a ningún trabajo.")
            else -> LazyColumn {
                items(appliedJobs) { job ->
                    Card(
                        elevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(job.title, style = MaterialTheme.typography.h6)
                            Text("📍 ${job.location}")
                            Text("🕒 ${job.dateTime}")
                            Spacer(Modifier.height(8.dp))
                            Text(job.description)

                            Spacer(Modifier.height(8.dp))

                            // Estado actual
                            when {
                                job.selectedWorkerId == currentUserId && job.isCompleted -> {
                                    Text("✅ Trabajo completado", color = Color.Green)
                                }
                                job.selectedWorkerId == currentUserId -> {
                                    Text("✅ Fuiste seleccionado", color = Color.Green)
                                }
                                job.selectedWorkerId != null -> {
                                    Text("❌ No fuiste seleccionado", color = Color.Red)
                                }
                                else -> {
                                    Text("⏳ En espera de decisión", color = Color.Gray)
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // Reseñar al contratista si fuiste seleccionado y se completó el trabajo
                            if (job.selectedWorkerId == currentUserId && job.isCompleted) {
                                var hasReviewed by remember { mutableStateOf(false) }

                                LaunchedEffect(job.id) {
                                    reviewRepo.hasReviewed(job.id, currentUserId, job.userId)
                                        .onSuccess { hasReviewed = it }
                                }

                                if (!hasReviewed) {
                                    Button(
                                        onClick = {
                                            navController.navigate("review/${job.id}/${job.userId}")
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Valorar al contratista")
                                    }
                                } else {
                                    Text("Ya valoraste al contratista", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
