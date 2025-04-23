package com.example.tfg_manitas.features.jobs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController

@Composable
fun JobListScreen(
    navController: NavHostController,
    jobViewModel: JobViewModel = viewModel()
) {
    val jobs by jobViewModel.userJobs.collectAsState()
    val isLoading by jobViewModel.isLoading.collectAsState()
    val error by jobViewModel.error.collectAsState()
    val context = LocalContext.current


    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Mis Trabajos Publicados", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (!error.isNullOrEmpty()) {
            Text("Error: $error", color = MaterialTheme.colors.error)
        } else if (jobs.isEmpty()) {
            Text("No has publicado ningún trabajo aún.")
        } else {
            LazyColumn {
                items(jobs) { job ->
                    var expanded by remember { mutableStateOf(false) }

                    Card(
                        elevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = job.title, style = MaterialTheme.typography.h6)
                                Box {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(onClick = {
                                            expanded = false
                                            navController.navigate("editJob/${job.id}")
                                        }) {
                                            Text("Editar")
                                        }
                                        DropdownMenuItem(onClick = {
                                            expanded = false
                                            jobViewModel.deleteJob(job.id)
                                        }) {
                                            Text("Eliminar")
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("📍 ${job.location}")
                            Text("🕒 ${job.dateTime}")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = job.description, maxLines = 2)
                        }
                    }
                }

            }
        }
    }
}
