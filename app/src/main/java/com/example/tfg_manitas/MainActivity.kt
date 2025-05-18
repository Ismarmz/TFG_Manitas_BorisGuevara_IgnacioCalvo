package com.example.tfg_manitas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.tfg_manitas.navigation.AppNavigation
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser

            // ✅ Determinar la pantalla inicial según el estado de verificación
            val startDestination = when {
                user == null -> "login"
                !user.isEmailVerified -> "verifyEmail"
                else -> "Main"
            }

            // ✅ Navegación de toda la app con destino seguro
            AppNavigation(navController = navController, startDestination = startDestination)
        }
    }
}
