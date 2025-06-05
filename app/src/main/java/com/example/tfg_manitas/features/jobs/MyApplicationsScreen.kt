package com.example.tfg_manitas.features.jobs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


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

    val appliedJobs = allJobs.filter { it.applicants.contains(currentUserId) }
    val selected = appliedJobs.filter { it.selectedWorkerId == currentUserId && !it.isCompleted }
    val pending = appliedJobs.filter { it.selectedWorkerId.isNullOrBlank() }
    val rejected = appliedJobs.filter { !it.selectedWorkerId.isNullOrBlank() && it.selectedWorkerId != currentUserId }
    val completed = appliedJobs.filter { it.selectedWorkerId == currentUserId && it.isCompleted }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(0xFFFFF8F0))
            .padding(16.dp)
    )
 {
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
            else -> {
                ApplicationCarousel("Fuiste seleccionado", selected, currentUserId, navController, reviewRepo)
                ApplicationCarousel("En espera", pending, currentUserId, navController, reviewRepo)
                ApplicationCarousel("No seleccionado", rejected, currentUserId, navController, reviewRepo)
                ApplicationCarousel("Completados", completed, currentUserId, navController, reviewRepo)
            }
        }
    }
}

@Composable
fun ApplicationCarousel(
    title: String,
    jobs: List<Job>,
    currentUserId: String,
    navController: NavHostController,
    reviewRepo: ReviewRepository
) {
    if (jobs.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2F4C5A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(jobs) { job ->
                    JobCardHorizontal(job, currentUserId, navController, reviewRepo)
                }
            }
        }
    }
}

@Composable
fun JobCardHorizontal(
    job: Job,
    currentUserId: String,
    navController: NavHostController,
    reviewRepo: ReviewRepository
) {
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
        else -> "En espera de decisión"
    }

    Card(
        modifier = Modifier
            .width(280.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE6F6))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = job.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2F4C5A)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = job.location, style = MaterialTheme.typography.bodySmall)
            Text(text = job.dateTime, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = job.description,
                maxLines = 2,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                job.selectedWorkerId == currentUserId -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, VerdeExito, RoundedCornerShape(6.dp))
                            .background(VerdeExito.copy(alpha = 0.1f))
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Estado",
                                tint = VerdeExito,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = estadoTexto.removePrefix("✅ "),
                                color = VerdeExito,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                job.selectedWorkerId.isNullOrBlank() -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF2F4C5A),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = estadoTexto,
                            color = Color(0xFFF4A950),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                else -> {
                    Text(
                        text = estadoTexto,
                        color = estadoColor,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Valoración realizada",
                                tint = Color(0xFF2F4C5A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Ya valoraste al contratista",
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFF2F4C5A)
                            )
                        }
                    }
                }
            }
        }
    }
}
