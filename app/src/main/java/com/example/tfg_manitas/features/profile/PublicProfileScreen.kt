package com.example.tfg_manitas.features.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tfg_manitas.R
import com.example.tfg_manitas.data.repository.ReviewRepository
import com.example.tfg_manitas.data.repository.UserRepository
import com.example.tfg_manitas.features.reviews.Review

@Composable
fun PublicProfileScreen(
    userId: String,
    navController: NavController
) {
    val reviewRepo = remember { ReviewRepository() }
    var user by remember { mutableStateOf<User?>(null) }
    var isUserLoading by remember { mutableStateOf(true) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isReviewsLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        val userResult = UserRepository().getUserById(userId)
        userResult.onSuccess { user = it }
        isUserLoading = false

        val reviewResult = reviewRepo.getReviewsForUser(userId)
        reviewResult.onSuccess { reviews = it }
        isReviewsLoading = false
    }

    if (isUserLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Usuario no encontrado")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen de perfil
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user!!.name.ifBlank { "Sin nombre" },
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = user!!.description.ifBlank { "Sin descripción" },
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Resumen de reputación
            if (!isReviewsLoading) {
                val totalReseñas = reviews.size
                val promedio = if (totalReseñas > 0) {
                    reviews.map { it.rating }.average()
                } else 0.0

                val etiqueta = when {
                    promedio >= 4.5 -> "Excelente reputación"
                    promedio >= 3.5 -> "Buena reputación"
                    promedio >= 2.5 -> "Reputación media"
                    promedio > 0 -> "Reputación baja"
                    else -> "Sin valoraciones aún"
                }

                Column(Modifier.padding(16.dp)) {
                    Text("Reputación", style = MaterialTheme.typography.h6)
                    Spacer(Modifier.height(4.dp))
                    Text("⭐ Promedio: ${"%.1f".format(promedio)} de 5.0")
                    Text("🗂 Total de reseñas: $totalReseñas")
                    Text("📊 $etiqueta")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de reseñas con trabajos
            UserReviewsSection(userId = user!!.uid)
        }
    }
}
