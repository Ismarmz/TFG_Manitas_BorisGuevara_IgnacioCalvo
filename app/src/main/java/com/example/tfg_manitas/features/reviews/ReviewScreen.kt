package com.example.tfg_manitas.features.reviews

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F0)) // fondo claro
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Deja tu valoración",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF2F4C5A),
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "${rating.toInt()} estrellas",
                color = Color(0xFF2F4C5A),
                style = MaterialTheme.typography.bodyLarge
            )

            Slider(
                value = rating,
                onValueChange = { rating = it },
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFF4A950),
                    activeTrackColor = Color(0xFF2F4C5A),
                    inactiveTrackColor = Color.LightGray
                )
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Comentario") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            errorMsg?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
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
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF4A950),
                    contentColor = Color(0xFF2F4C5A)
                )
            ) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(24.dp))
                else Text("Enviar reseña")
            }
        }
    }
}
