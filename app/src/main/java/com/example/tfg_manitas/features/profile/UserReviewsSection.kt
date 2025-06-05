package com.example.tfg_manitas.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
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

    var sortOrder by remember { mutableStateOf("desc") }

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
        val sortedReviews = reviews.sortedBy {
            if (sortOrder == "asc") it.rating else -it.rating
        }

        val reseñasFiltradas = sortedReviews.filter { review ->
            val job = jobs.find { it.id == review.jobId }
            job?.userId == userId || job?.selectedWorkerId == userId
        }

        var expanded by remember { mutableStateOf(false) }

        LazyColumn {
            item {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text("Ordenar por", color = Color(0xFF2F4C5A))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Ordenar",
                                tint = Color(0xFF2F4C5A)
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(onClick = {
                                sortOrder = "desc"
                                expanded = false
                            }) {
                                Text("Mayor puntuación")
                            }
                            DropdownMenuItem(onClick = {
                                sortOrder = "asc"
                                expanded = false
                            }) {
                                Text("Menor puntuación")
                            }
                        }
                    }
                }
            }

            items(reseñasFiltradas) { review ->
                val job = jobs.find { it.id == review.jobId }
                if (job != null && job.isCompleted) {
                    ReviewCard(review = review, job = job, viewerUserId = userId)
                }
            }
        }
    }
}

@Composable
fun ReviewCard(review: Review, job: Job, viewerUserId: String) {
    val rol = when (review.toUserId) {
        job.selectedWorkerId -> "Trabajador"
        job.userId -> "Contratista"
        else -> "Usuario"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = job.title,
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2F4C5A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Rol: $rol",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Puntuación: ${review.rating}",
                        style = MaterialTheme.typography.body1
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "★",
                        style = MaterialTheme.typography.body1,
                        color = Color(0xFFF4A950)
                    )
                }

                if (!review.comment.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${review.comment}\"",
                        style = MaterialTheme.typography.body2,
                        color = Color(0xFF2F4C5A)
                    )
                }
            }
        }
    }
}
