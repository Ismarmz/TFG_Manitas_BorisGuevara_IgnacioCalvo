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

            // Verificar si el usuario ya está logueado
            val startDestination = if (auth.currentUser != null) "Main" else "login"

            AppNavigation(navController, startDestination)
        }
    }
}
