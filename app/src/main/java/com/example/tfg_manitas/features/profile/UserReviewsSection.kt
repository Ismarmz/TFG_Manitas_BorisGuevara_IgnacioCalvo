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
                reseñasComoTrabajador.forEach { review ->
                    val job = jobs.find { it.id == review.jobId }
                    if (job != null && job.isCompleted) {
                        ReviewCard(review = review, job = job, viewerUserId = userId)
                    }
                }

                reseñasComoContratista.forEach { review ->
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
                .padding(vertical = 6.dp),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = job.title,
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.primary
                )
                Text("Rol: $rol", style = MaterialTheme.typography.caption)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Puntuación: ${review.rating} ★", style = MaterialTheme.typography.body2)
                if (!review.comment.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("\"${review.comment}\"", style = MaterialTheme.typography.body2)
                }
            }
        }
    }

