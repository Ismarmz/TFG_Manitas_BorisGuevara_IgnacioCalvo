package com.example.tfg_manitas.features.jobs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.TextButton
import androidx.compose.material3.*
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
import com.example.tfg_manitas.data.repository.ReviewRepository
import com.example.tfg_manitas.ui.theme.VerdeExito
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

    val appliedJobs = allJobs.filter {
        it.applicants.contains(currentUserId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F0))
            .padding(16.dp)
    ) {
        Text(
            text = "Mis Postulaciones",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = Color(0xFF2F4C5A),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(16.dp))

        when {
            isLoading -> CircularProgressIndicator()
            !error.isNullOrEmpty() -> Text("Error: $error", color = Color.Red)
            appliedJobs.isEmpty() -> Text("Aún no te has postulado a ningún trabajo.")
            else -> LazyColumn {
                items(appliedJobs) { job ->

                    val estadoColor = when {
                        job.selectedWorkerId == currentUserId && job.isCompleted -> Color(0xFF2E7D32)
                        job.selectedWorkerId == currentUserId -> Color(0xFF2F4C5A)
                        job.selectedWorkerId != null -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.secondary
                    }

                    val estadoTexto = when {
                        job.selectedWorkerId == currentUserId && job.isCompleted -> "✅ Trabajo completado"
                        job.selectedWorkerId == currentUserId -> "✅ Fuiste seleccionado"
                        !job.selectedWorkerId.isNullOrBlank() -> "❌ No fuiste seleccionado"
                        else -> "⏳ En espera de decisión"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE6F6))
                    ) {
                        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFF2F4C5A))
                            )

                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = job.title,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2F4C5A)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = job.tags.joinToString(", "),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "📍 ${job.location}  |  🕒 ${job.dateTime}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = job.description,
                                    maxLines = 2,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                if (job.selectedWorkerId == currentUserId) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(1.dp, VerdeExito, RoundedCornerShape(6.dp))
                                            .background(VerdeExito.copy(alpha = 0.1f))
                                            .padding(vertical = 8.dp, horizontal = 12.dp)
                                    ) {
                                        Text(
                                            text = estadoTexto,
                                            color = VerdeExito,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else {
                                    Text(
                                        text = estadoTexto,
                                        color = estadoColor,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

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
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFF4A950),
                                                contentColor = Color(0xFF2F4C5A)
                                            )
                                        ) {
                                            Text("Valorar al contratista")
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp)
                                                .background(
                                                    color = Color(0xFFF4A950).copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                        ) {
                                            Text(
                                                text = "✅ Ya valoraste al contratista",
                                                modifier = Modifier.padding(8.dp),
                                                color = Color(0xFF2F4C5A)
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
    }
}