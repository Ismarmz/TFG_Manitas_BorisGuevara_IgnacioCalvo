package com.example.tfg_manitas.features.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun VerifyEmailScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Verifica tu correo", style = MaterialTheme.typography.h5)
        Spacer(Modifier.height(16.dp))
        Text(
            "Te hemos enviado un correo electrónico de verificación a:\n${user?.email}",
            style = MaterialTheme.typography.body1
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                user?.sendEmailVerification()
                Toast.makeText(context, "Correo reenviado", Toast.LENGTH_SHORT).show()
            }
        ) {
            Text("Reenviar correo")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    user?.reload()
                    if (user?.isEmailVerified == true) {
                        Toast.makeText(context, "Correo verificado", Toast.LENGTH_SHORT).show()
                        navController.navigate("completeProfile") {
                            popUpTo("verifyEmail") { inclusive = true }
                        }
                    } else {
                        Toast.makeText(context, "Aún no has verificado tu correo", Toast.LENGTH_SHORT).show()
                    }
                    isLoading = false
                }
            },
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(Modifier.size(20.dp))
            else Text("Ya verifiqué")
        }
    }
}
