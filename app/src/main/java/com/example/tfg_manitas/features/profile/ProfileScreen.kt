package com.example.tfg_manitas.features.profile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tfg_manitas.R
import com.example.tfg_manitas.data.repository.ReviewRepository
import com.example.tfg_manitas.data.repository.UserRepository
import com.example.tfg_manitas.features.reviews.Review
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.BarChart

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var user by remember { mutableStateOf<User?>(null) }
    var isUserLoading by remember { mutableStateOf(true) }
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val reviewRepo = remember { ReviewRepository() }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isReviewsLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val userResult = UserRepository().getUserById(userId)
        userResult.onSuccess {
            user = it
            name = it.name
            description = it.description
        }
        isUserLoading = false

        val reviewResult = reviewRepo.getReviewsForUser(userId)
        reviewResult.onSuccess { reviews = it }
        isReviewsLoading = false
    }

    if (isUserLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF8F0)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF8F0))
        ) {
            // Menú flotante en la esquina superior derecha
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menú")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        expanded = false
                        isEditing = true
                    }) {
                        Text("Editar perfil")
                    }
                    DropdownMenuItem(onClick = {
                        expanded = false
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                        onLogout()
                    }) {
                        Text("Cerrar sesión")
                    }
                }
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
                    text = name.ifBlank { "Sin nombre" },
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre completo") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                if (name.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "El nombre no puede estar vacío",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                val updatedUser = user?.copy(
                                    name = name,
                                    description = description
                                ) ?: return@Button

                                coroutineScope.launch {
                                    val result = UserRepository().updateUser(updatedUser)
                                    if (result.isSuccess) {
                                        Toast.makeText(
                                            context,
                                            "Perfil actualizado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        user = updatedUser
                                        isEditing = false
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error al actualizar",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Guardar")
                        }
                    }
                } else {
                    Text(
                        text = description.ifBlank { "Sin descripción" },
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

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
                        promedio >= 4.5 -> Color(0xFF10B981)  // Verde
                        promedio >= 3.5 -> Color(0xFF6EE7B7)  // Verde claro
                        promedio >= 2.5 -> Color(0xFFFBBF24)  // Amarillo
                        promedio > 0 -> Color(0xFFF87171)     // Rojo claro
                        else -> Color.Gray
                    }

                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val iconColor = Color(0xFF2F4C5A)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Caja 1: Promedio
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
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Promedio",
                                            tint = iconColor
                                        )
                                        Text(" ${"%.1f".format(promedio)}")
                                    }
                                }
                            }

                            Divider(
                                color = Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(1.dp)
                            )

                            // Caja 2: Total reseñas
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
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = "Total reseñas",
                                            tint = iconColor
                                        )
                                        Text("Reseñas: $totalReseñas")
                                    }
                                }
                            }

                            Divider(
                                color = Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(1.dp)
                            )

                            // Caja 3: Reputación
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
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BarChart,
                                            contentDescription = "Reputación",
                                            tint = iconColor
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

                    Spacer(modifier = Modifier.height(16.dp))
                    UserReviewsSection(userId = userId)
                }
            }
        }
    }
}

