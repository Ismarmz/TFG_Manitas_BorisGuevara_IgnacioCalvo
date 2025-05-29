package com.example.tfg_manitas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.tfg_manitas.navigation.AppNavigation
import com.example.tfg_manitas.ui.theme.TFG_ManitasTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TFG_ManitasTheme {
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

        // Crear canal de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Chat"
            val descriptionText = "Notificaciones de nuevos mensajes"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("chat_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Guardar token FCM al iniciar la app
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(currentUserId).update("fcmToken", token)
                }
            }
        }
    }
}
git add .
