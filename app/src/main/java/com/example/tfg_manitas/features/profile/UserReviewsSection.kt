package com.example.tfg_manitas.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tfg_manitas.data.repository.JobRepository
import com.example.tfg_manitas.data.repository.ReviewRepository
import com.example.tfg_manitas.features.jobs.Job
import com.example.tfg_manitas.features.reviews.Review

@Composable
fun UserReviewsSection(userId: String) {
    val reviewRepo = remember { ReviewRepository() }
    val jobRepo = remember { JobRepository() }

    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var jobs by remember { mutableStateOf<List<Job>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        val reviewResult = reviewRepo.getReviewsForUser(userId)
        reviewResult.onSuccess { reviews = it }

        jobRepo.listenToAllJobs().collect {
            jobs = it
            isLoading = false
        }
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else if (reviews.isEmpty()) {
        Text("Aún no tiene reseñas", style = MaterialTheme.typography.body2)
    } else {
        val reseñasComoTrabajador = reviews.filter { review ->
            val job = jobs.find { it.id == review.jobId }
            job?.selectedWorkerId == userId
        }

        val reseñasComoContratista = reviews.filter { review ->
            val job = jobs.find { it.id == review.jobId }
            job?.userId == userId
        }

        Column {
            if (reseñasComoTrabajador.isNotEmpty()) {
                Text("⭐ Reseñas como trabajador", style = MaterialTheme.typography.subtitle1)
                Spacer(Modifier.height(8.dp))
                reseñasComoTrabajador.forEach { review ->
                    ReviewCard(review)
                }
                Spacer(Modifier.height(16.dp))
            }

            if (reseñasComoContratista.isNotEmpty()) {
                Text("👷 Reseñas como contratista", style = MaterialTheme.typography.subtitle1)
                Spacer(Modifier.height(8.dp))
                reseñasComoContratista.forEach { review ->
                    ReviewCard(review)
                }
            }
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Text("⭐ ${review.rating}", style = MaterialTheme.typography.body1)
        if (review.comment.isNotBlank()) {
            Text(review.comment, style = MaterialTheme.typography.body2)
        }
        Divider(modifier = Modifier.padding(vertical = 4.dp))
    }
}
