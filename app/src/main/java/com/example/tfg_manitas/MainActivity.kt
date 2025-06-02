package com.example.tfg_manitas

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import com.example.tfg_manitas.navigation.AppNavigation
import com.example.tfg_manitas.ui.theme.TFG_ManitasTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val incomingIntentState = mutableStateOf<Intent?>(null)

    @OptIn(UnstableApi::class)
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

                val intentState = incomingIntentState.value

                LaunchedEffect(intentState) {
                    val jobId = intentState?.getStringExtra("jobId")
                    val otherUserId = intentState?.getStringExtra("otherUserId")

                    if (!jobId.isNullOrEmpty() && !otherUserId.isNullOrEmpty()) {
                        Log.d("NAVIGATION", "Ir a chat/$jobId/$otherUserId")
                        navController.navigate("chat/$jobId/$otherUserId") {
                            popUpTo("Main") { inclusive = false }
                            launchSingleTop = true
                        }
                        incomingIntentState.value = null // evitar redirección múltiple
                    }
                }
            }
        }

        // Asignar intent inicial
        incomingIntentState.value = intent

        // Solicitar permisos para notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        // Crear canal de notificación
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

        // Guardar token FCM
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

    @OptIn(UnstableApi::class)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        incomingIntentState.value = intent
        Log.d("ON_NEW_INTENT", "INTENT RECIBIDO: jobId=${intent.getStringExtra("jobId")}, otherUserId=${intent.getStringExtra("otherUserId")}")
    }
}
