package com.example.tfg_manitas.features.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF8F0))
        ) {
            // ← Botón de volver (visible ahora)
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF2F4C5A)
                )
            }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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

                    if (!isReviewsLoading) {
                        val totalReseñas = reviews.size
                        val promedio = if (totalReseñas > 0) {
                            reviews.map { it.rating }.average()
                        } else 0.0

                        val etiqueta = when {
                            promedio >= 4.5 -> "Reputación Excelente"
                            promedio >= 3.5 -> "Reputación Buena"
                            promedio >= 2.5 -> "Reputación Media"
                            promedio > 0 -> "Reputación Baja"
                            else -> "Sin valoraciones aún"
                        }

                        val etiquetaColor = when {
                            promedio >= 4.5 -> Color(0xFF10B981)
                            promedio >= 3.5 -> Color(0xFF6EE7B7)
                            promedio >= 2.5 -> Color(0xFFFBBF24)
                            promedio > 0 -> Color(0xFFF87171)
                            else -> Color.Gray
                        }

                        Column(Modifier.padding(horizontal = 16.dp)) {

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = 0.dp,
                                        backgroundColor = Color(0xFFFFF8F0),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Promedio",
                                                    tint = Color(0xFF2F4C5A)
                                                )
                                                Text("${"%.1f".format(promedio)}")
                                            }
                                        }
                                    }
                                }

                                Divider(
                                    color = Color.Gray.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .height(40.dp)
                                        .width(1.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = 0.dp,
                                        backgroundColor = Color(0xFFFFF8F0),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = Icons.Default.Folder,
                                                    contentDescription = "Total reseñas",
                                                    tint = Color(0xFF2F4C5A)
                                                )
                                                Text("Reseñas: $totalReseñas")
                                            }
                                        }
                                    }
                                }

                                Divider(
                                    color = Color.Gray.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .height(40.dp)
                                        .width(1.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = 0.dp,
                                        backgroundColor = Color(0xFFFFF8F0),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        imageVector = Icons.Default.BarChart,
                                                        contentDescription = "Reputación",
                                                        tint = Color(0xFF2F4C5A)
                                                    )
                                                    Text(
                                                        text = etiqueta.replace("Reputación ", "")
                                                            .replace("reputación ", ""),
                                                        color = etiquetaColor,
                                                        style = MaterialTheme.typography.body2,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    UserReviewsSection(userId = user!!.uid)
                }
            }
        }
    }
