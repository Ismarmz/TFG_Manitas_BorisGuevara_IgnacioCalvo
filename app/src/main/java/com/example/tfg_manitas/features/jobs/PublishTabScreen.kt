package com.example.tfg_manitas.features.jobs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            FloatingActionButton(
                onClick = { navController.navigate("post") },
                backgroundColor = Color(0xFF2F4C5A),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Publicar nuevo trabajo")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF8F0))
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Mis Trabajos Publicados",
                style = MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = Color(0xFF2F4C5A),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

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
                            shape = RoundedCornerShape(16.dp),
                            elevation = 8.dp,
                            backgroundColor = Color(0xFFEDE6F6)
                        ) {
                            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                                Box(
                                    modifier = Modifier
                                        .width(8.dp)
                                        .fillMaxHeight()
                                        .background(Color(0xFF2F4C5A))
                                )

                                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = job.title,
                                            style = MaterialTheme.typography.h6.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color(0xFF2F4C5A)
                                        )

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

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = job.description,
                                        style = MaterialTheme.typography.body1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val iconColor = Color(0xFF2F4C5A)

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = "Ubicación",
                                            tint = iconColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = job.location, style = MaterialTheme.typography.body2)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = "Fecha y hora",
                                            tint = iconColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = job.dateTime, style = MaterialTheme.typography.body2)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AttachMoney,
                                            contentDescription = "Pago",
                                            tint = iconColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = job.paymentAmount, style = MaterialTheme.typography.body2)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

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

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                TextButton(onClick = {
                                                    navController.navigate("publicProfile/$applicantId")
                                                }) {
                                                    Text(
                                                        text = applicantName ?: "Cargando...",
                                                        color = Color(0xFFF4A950),
                                                        style = MaterialTheme.typography.body1
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(0.9f),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
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
                                                            enabled = !isPreselected,
                                                            modifier = Modifier.weight(1f),
                                                            colors = ButtonDefaults.buttonColors(
                                                                backgroundColor = Color(0xFFF4A950),
                                                                contentColor = Color(0xFF2F4C5A)
                                                            ),
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                        ) {
                                                            Text(if (isPreselected) "Chatear" else "Preseleccionar", fontSize = 13.sp)
                                                        }
                                                    }

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
                                                        enabled = job.selectedWorkerId == null,
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.buttonColors(
                                                            backgroundColor = Color(0xFFF4A950),
                                                            contentColor = Color(0xFF2F4C5A)
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                    ) {
                                                        Text("Asignar", fontSize = 13.sp)
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

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF10B981).copy(alpha = 0.1f))
                                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Asignado",
                                                        tint = Color(0xFF10B981),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Asignado a: ${selectedName ?: job.selectedWorkerId}",
                                                        color = Color(0xFF10B981),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
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
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                backgroundColor = Color(0xFFF4A950),
                                                contentColor = Color(0xFF2F4C5A)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                                        ) {
                                            Text("Marcar como completado", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        }
                                    } else if (job.isCompleted) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(6.dp))
                                                .background(Color(0xFF10B981).copy(alpha = 0.1f))
                                                .padding(vertical = 8.dp, horizontal = 12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Trabajo completado",
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Trabajo completado",
                                                    color = Color(0xFF10B981),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

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