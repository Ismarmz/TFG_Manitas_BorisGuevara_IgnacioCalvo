package com.example.tfg_manitas.features.jobs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
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
fun PublishTabScreen(navController: NavHostController) {
    val jobViewModel: JobViewModel = viewModel()
    val userJobs by jobViewModel.userJobs.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("post")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Publicar nuevo trabajo")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Mis Trabajos Publicados", style = MaterialTheme.typography.h5)
            Spacer(Modifier.height(16.dp))

            if (userJobs.isEmpty()) {
                Text("Aún no has publicado trabajos.")
            } else {
                LazyColumn {
                    items(userJobs) { job ->
                        var expandedMenu by remember { mutableStateOf(false) }
                        var selectedName by remember { mutableStateOf<String?>(null) }
                        var showDeleteDialog by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(job.title, style = MaterialTheme.typography.h6)

                                    Box {
                                        IconButton(onClick = { expandedMenu = true }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                                        }

                                        DropdownMenu(
                                            expanded = expandedMenu,
                                            onDismissRequest = { expandedMenu = false }
                                        ) {
                                            DropdownMenuItem(onClick = {
                                                expandedMenu = false
                                                navController.navigate("editJob/${job.id}")
                                            }) {
                                                Text("Editar")
                                            }

                                            DropdownMenuItem(onClick = {
                                                expandedMenu = false
                                                showDeleteDialog = true
                                            }) {
                                                Text("Eliminar")
                                            }
                                        }
                                    }
                                }

                                Text(job.description)
                                Spacer(Modifier.height(4.dp))
                                Text("📍 ${job.location}")
                                Text("🕒 ${job.dateTime}")

                                Spacer(Modifier.height(8.dp))

                                if (job.applicants.isNotEmpty()) {
                                    Text("Postulantes:", style = MaterialTheme.typography.subtitle2)

                                    job.applicants.forEach { applicantId ->
                                        var applicantName by remember { mutableStateOf<String?>(null) }

                                        LaunchedEffect(applicantId) {
                                            val res = UserRepository().getUserById(applicantId)
                                            applicantName = res.getOrNull()?.name ?: "Usuario"
                                        }

                                        val isPreselected = job.shortlistedWorkerIds.contains(applicantId)
                                        val isFinal = job.selectedWorkerId == applicantId

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextButton(onClick = {
                                                navController.navigate("publicProfile/$applicantId")
                                            }) {
                                                Text(applicantName ?: "Cargando...")
                                            }

                                            Row {
                                                if (!isFinal) {
                                                    Button(
                                                        onClick = {
                                                            coroutineScope.launch {
                                                                jobViewModel.shortlistWorker(
                                                                    jobId = job.id,
                                                                    workerId = applicantId,
                                                                    onSuccess = {
                                                                        Toast.makeText(context, "Preseleccionado y chat abierto", Toast.LENGTH_SHORT).show()
                                                                        navController.navigate("chat/${job.id}/$applicantId")
                                                                        jobViewModel.refreshJobs()
                                                                    },
                                                                    onFailure = { error ->
                                                                        Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                )
                                                            }
                                                        },
                                                        enabled = !isPreselected
                                                    ) {
                                                        Text(if (isPreselected) "Chatear" else "Preseleccionar y chatear")
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Button(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            jobViewModel.selectWorkerAndCreateChat(
                                                                jobId = job.id,
                                                                workerId = applicantId,
                                                                onSuccess = {
                                                                    Toast.makeText(context, "Trabajador asignado", Toast.LENGTH_SHORT).show()
                                                                    jobViewModel.refreshJobs()
                                                                },
                                                                onFailure = { error ->
                                                                    Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                                                                }
                                                            )
                                                        }
                                                    },
                                                    enabled = job.selectedWorkerId == null
                                                ) {
                                                    Text("Asignar")
                                                }
                                            }
                                        }
                                    }

                                    if (job.selectedWorkerId != null) {
                                        LaunchedEffect(job.selectedWorkerId) {
                                            if (selectedName == null) {
                                                val res = UserRepository().getUserById(job.selectedWorkerId!!)
                                                selectedName = res.getOrNull()?.name ?: "Usuario"
                                            }
                                        }

                                        Text(
                                            "✅ Asignado a: ${selectedName ?: job.selectedWorkerId}",
                                            color = Color.Green
                                        )
                                    }
                                } else {
                                    Text("Sin postulantes aún", color = Color.Gray)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (job.userId == userId && job.selectedWorkerId != null && !job.isCompleted) {
                                    Button(
                                        onClick = {
                                            jobViewModel.updateJob(
                                                jobId = job.id,
                                                updatedJob = job.copy(isCompleted = true),
                                                onSuccess = {
                                                    Toast.makeText(context, "Trabajo completado", Toast.LENGTH_SHORT).show()
                                                    jobViewModel.refreshJobs()
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
                                    Text(
                                        text = "✅ Trabajo completado",
                                        style = MaterialTheme.typography.subtitle2,
                                        color = Color.Green,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

// AlertDialog de confirmación de eliminación
                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Confirmar eliminación") },
                                text = { Text("¿Estás seguro de que quieres eliminar este trabajo? Esta acción no se puede deshacer.") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        coroutineScope.launch {
                                            jobViewModel.deleteJobIfAllowed(job.id) { success, error ->
                                                if (success) {
                                                    Toast.makeText(context, "Trabajo eliminado", Toast.LENGTH_SHORT).show()
                                                    jobViewModel.refreshJobs()
                                                } else {
                                                    Toast.makeText(context, error ?: "No se pudo eliminar el trabajo", Toast.LENGTH_LONG).show()
                                                }
                                                showDeleteDialog = false
                                            }
                                        }
                                    }) {
                                        Text("Eliminar", color = Color.Red)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text("Cancelar")
                                    }
                                }
                            )
                        }

                    }
                }
            }
        }
    }
}
