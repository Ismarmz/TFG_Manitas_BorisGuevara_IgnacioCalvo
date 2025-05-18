package com.example.tfg_manitas.features.jobs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.tfg_manitas.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun JobsListScreen(
    jobViewModel: JobViewModel = viewModel(),
    navController: NavHostController       // <-- Recibimos el NavController
) {
    val userJobs by jobViewModel.userJobs.collectAsState()
    val isLoading by jobViewModel.isLoading.collectAsState()
    val error by jobViewModel.error.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mis Trabajos Publicados", style = MaterialTheme.typography.h5)

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            !error.isNullOrEmpty() -> {
                Text("Error: $error", color = Color.Red)
            }
            userJobs.isEmpty() -> {
                Text("No has publicado ningún trabajo aún.")
            }
            else -> {
                LazyColumn {
                    items(userJobs) { job ->
                        Card(
                            elevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = job.title, style = MaterialTheme.typography.h6)
                                Text("📍 ${job.location}")
                                Text("🕒 ${job.dateTime}")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = job.description)

                                Spacer(modifier = Modifier.height(8.dp))

                                // Postulantes y selección
                                if (job.applicants.isNotEmpty()) {
                                    Text("Postulantes:", style = MaterialTheme.typography.subtitle2)
                                    job.applicants.forEach { applicantId ->
                                        var applicantName by remember { mutableStateOf<String?>(null) }

                                        // Cargar nombre una sola vez
                                        LaunchedEffect(applicantId) {
                                            val res = UserRepository().getUserById(applicantId)
                                            applicantName = res.getOrNull()?.name ?: "Usuario"
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(applicantName ?: "Cargando...", style = MaterialTheme.typography.body2)

                                            Button(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        val result = jobViewModel.selectWorker(job.id, applicantId)
                                                        if (result.isSuccess) {
                                                            Toast.makeText(context, "Trabajador asignado", Toast.LENGTH_SHORT).show()
                                                            jobViewModel.refreshJobs()
                                                        } else {
                                                            Toast.makeText(context, "Error al asignar", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                enabled = job.selectedWorkerId == null
                                            ) {
                                                Text("Seleccionar")
                                            }
                                        }
                                    }
                                    if (job.selectedWorkerId != null) {
                                        Text("✅ Asignado a: ${job.selectedWorkerId}", color = Color.Green)
                                    }
                                } else {
                                    Text("Sin postulantes aún", color = Color.Gray)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Botón Eliminar
                                Button(
                                    onClick = { jobViewModel.deleteJob(job.id) },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                                ) {
                                    Text("Eliminar trabajo", color = Color.White)
                                }

                                // Botón “Marcar como completado” y navegación a reseña
                                if (job.userId == userId && job.selectedWorkerId != null && !job.isCompleted) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            jobViewModel.updateJob(
                                                jobId = job.id,
                                                updatedJob = job.copy(isCompleted = true),
                                                onSuccess = {
                                                    Toast.makeText(context, "Trabajo completado", Toast.LENGTH_SHORT).show()
                                                    jobViewModel.refreshJobs()
                                                    // Navegar a ReviewScreen justo después
                                                    navController.navigate("review/${job.id}/${job.selectedWorkerId}")
                                                },
                                                onFailure = { errorMsg ->
                                                    Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Marcar como completado")
                                    }
                                } else if (job.isCompleted) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "✅ Trabajo completado",
                                        style = MaterialTheme.typography.subtitle2,
                                        color = Color.Green,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
