package com.example.tfg_manitas.features.reviews

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tfg_manitas.data.repository.ReviewRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ReviewScreen(
    jobId: String,
    toUserId: String,
    navController: NavController,
    reviewRepo: ReviewRepository = ReviewRepository()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var rating by remember { mutableStateOf(3f) }
    var comment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Deja tu valoración", style = MaterialTheme.typography.h5)
        Spacer(Modifier.height(16.dp))

        Text("${rating.toInt()} estrellas")
        Slider(
            value = rating,
            onValueChange = { rating = it },
            valueRange = 1f..5f,
            steps = 3
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Comentario") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        errorMsg?.let {
            Text(it, color = MaterialTheme.colors.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                if (toUserId == currentUid) {
                    errorMsg = "No puedes valorarte a ti mismo"
                    return@Button
                }

                if (rating.toInt() == 0 || comment.isBlank()) {
                    errorMsg = "Por favor completa la puntuación y deja un comentario"
                    return@Button
                }

                isSubmitting = true
                errorMsg = null

                scope.launch {
                    val has = reviewRepo.hasReviewed(jobId, currentUid, toUserId)
                    if (has.isSuccess && has.getOrNull() == true) {
                        errorMsg = "Ya has dejado una reseña para este trabajo"
                        isSubmitting = false
                        return@launch
                    }

                    val review = Review(
                        fromUserId = currentUid,
                        toUserId = toUserId,
                        jobId = jobId,
                        rating = rating.toDouble(),
                        comment = comment
                    )

                    val res = reviewRepo.submitReview(review)
                    isSubmitting = false

                    if (res.isSuccess) {
                        Toast.makeText(context, "Reseña enviada", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        errorMsg = "Error al enviar reseña"
                    }
                }
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSubmitting) CircularProgressIndicator(Modifier.size(24.dp))
            else Text("Enviar reseña")
        }
    }
}
