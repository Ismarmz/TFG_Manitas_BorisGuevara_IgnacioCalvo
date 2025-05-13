package com.example.tfg_manitas.features.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tfg_manitas.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import com.example.tfg_manitas.features.profile.User

@Composable
fun CompleteProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Completa tu perfil", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isBlank()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val newUser = User(
                        uid = user.uid,
                        email = user.email ?: "",
                        name = name,
                        description = description,
                        createdAt = LocalDateTime.now().toString()
                    )

                    isLoading = true
                    coroutineScope.launch {
                        val result = UserRepository().createUser(newUser)
                        isLoading = false
                        if (result.isSuccess) {
                            Toast.makeText(context, "Perfil guardado", Toast.LENGTH_SHORT).show()
                            navController.navigate("Main") {
                                popUpTo("completeProfile") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar perfil")
        }
    }
}
