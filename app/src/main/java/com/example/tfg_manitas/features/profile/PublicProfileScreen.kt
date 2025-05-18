package com.example.tfg_manitas.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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

    // Carga usuario y reseñas
    LaunchedEffect(userId) {
        println("👁️ Viendo perfil público de UID: $userId")

        val userResult = UserRepository().getUserById(userId)
        userResult.onSuccess {
            user = it
            println("👤 Usuario cargado: ${it.name}")
        }.onFailure {
            println("❌ Error cargando usuario: ${it.message}")
        }
        isUserLoading = false

        val reviewResult = reviewRepo.getReviewsForUser(userId)
        reviewResult.onSuccess {
            reviews = it
            println("✅ Se cargaron ${it.size} reseñas para $userId")
        }.onFailure {
            println("❌ Error cargando reseñas: ${it.message}")
        }
        isReviewsLoading = false
    }



    when {
        isUserLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        user == null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Usuario no encontrado")
            }
        }
        else -> {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                // Datos del usuario
                Text(user!!.name, style = MaterialTheme.typography.h5)
                Text(user!!.description.ifBlank { "Sin descripción" })
                Spacer(Modifier.height(16.dp))

                // Reseñas
                if (isReviewsLoading) {
                    CircularProgressIndicator()
                } else if (reviews.isEmpty()) {
                    Text("Aún no tiene reseñas", style = MaterialTheme.typography.body2)
                } else {
                    val avg = reviews.map { it.rating }.average()
                    Text("⭐ Promedio: %.1f".format(avg), style = MaterialTheme.typography.subtitle1)
                    Spacer(Modifier.height(12.dp))

                    LazyColumn {
                        items(reviews) { r ->
                            Text("De ${r.fromUserId}: ${r.rating} ★", style = MaterialTheme.typography.body1)
                            if (r.comment.isNotBlank()) {
                                Text(r.comment, style = MaterialTheme.typography.body2)
                            }
                            Divider(Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }
    }
}
