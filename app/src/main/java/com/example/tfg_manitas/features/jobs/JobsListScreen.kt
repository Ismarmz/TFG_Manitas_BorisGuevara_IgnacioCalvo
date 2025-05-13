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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun JobsListScreen(jobViewModel: JobViewModel = viewModel()) {
    val userJobs by jobViewModel.userJobs.collectAsState()
    val isLoading by jobViewModel.isLoading.collectAsState()
    val error by jobViewModel.error.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mis Trabajos Publicados", style = MaterialTheme.typography.h5)

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (!error.isNullOrEmpty()) {
            Text("Error: $error", color = Color.Red)
        } else if (userJobs.isEmpty()) {
            Text("No has publicado ningún trabajo aún.")
        } else {
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

                            if (job.applicants.isNotEmpty()) {
                                Text("Postulantes:", style = MaterialTheme.typography.subtitle2)

                                job.applicants.forEach { applicantId ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(applicantId, style = MaterialTheme.typography.body2)

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

                            Button(
                                onClick = {
                                    jobViewModel.deleteJob(job.id)
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                            ) {
                                Text("Eliminar trabajo", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
