package com.example.tfg_manitas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.tfg_manitas.navigation.AppNavigation
import com.example.tfg_manitas.ui.theme.TFG_ManitasTheme  // 👈 Asegúrate de importar esto
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TFG_ManitasTheme {  // ✅ Tema personalizado aplicado aquí
                val navController = rememberNavController()
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser

                val startDestination = when {
                    user == null -> "login"
                    !user.isEmailVerified -> "verifyEmail"
                    else -> "Main"
                }

                AppNavigation(navController = navController, startDestination = startDestination)
            }
        }
    }
}
