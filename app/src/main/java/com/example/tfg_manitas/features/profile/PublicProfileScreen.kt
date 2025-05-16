package com.example.tfg_manitas.features.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.tfg_manitas.data.repository.UserRepository
import com.example.tfg_manitas.features.profile.User
import kotlinx.coroutines.launch

@Composable
fun PublicProfileScreen(userId: String, navController: NavController) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        coroutineScope.launch {
            val result = UserRepository().getUserById(userId)
            user = result.getOrNull()
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 🔙 Botón de volver arriba a la izquierda
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
        }

        when {
            isLoading -> {
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 72.dp, start = 24.dp, end = 24.dp), // deja espacio al botón
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

                    Text(text = user!!.name, style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = user!!.description.ifBlank { "Sin descripción" },
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Miembro desde: ${user!!.createdAt.substringBefore('T')}",
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
}
