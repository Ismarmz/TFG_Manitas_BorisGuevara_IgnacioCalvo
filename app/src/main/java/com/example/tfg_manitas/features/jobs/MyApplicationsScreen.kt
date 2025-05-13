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
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MyApplicationsScreen(jobViewModel: JobViewModel = viewModel()) {
    val allJobs by jobViewModel.availableJobs.collectAsState()
    val isLoading by jobViewModel.isLoading.collectAsState()
    val error by jobViewModel.error.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    val appliedJobs = allJobs.filter { it.applicants.contains(userId) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mis Postulaciones", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (!error.isNullOrEmpty()) {
            Text("Error: $error", color = Color.Red)
        } else if (appliedJobs.isEmpty()) {
            Text("Aún no te has postulado a ningún trabajo.")
        } else {
            LazyColumn {
                items(appliedJobs) { job ->
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
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = job.description)

                            Spacer(modifier = Modifier.height(8.dp))

                            when {
                                job.selectedWorkerId == userId -> {
                                    Text("✅ Fuiste seleccionado", color = Color.Green)
                                }
                                job.selectedWorkerId != null && job.selectedWorkerId != userId -> {
                                    Text("❌ No fuiste seleccionado", color = Color.Red)
                                }
                                else -> {
                                    Text("⏳ En espera de decisión", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
