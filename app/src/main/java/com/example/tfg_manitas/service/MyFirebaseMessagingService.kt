package com.example.tfg_manitas.service

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.tfg_manitas.MainActivity
import com.example.tfg_manitas.R
import com.example.tfg_manitas.features.chat.ChatStateManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.HttpURLConnection
import java.net.URL

object NotificationCache {
    private val chatMessagesMap = mutableMapOf<String, MutableList<String>>()

    fun addMessage(chatId: String, message: String) {
        val list = chatMessagesMap.getOrPut(chatId) { mutableListOf() }
        list.add(message)
        if (list.size > 5) list.removeAt(0)
    }

    fun getMessages(chatId: String): List<String> {
        return chatMessagesMap[chatId] ?: emptyList()
    }

    fun clear(chatId: String) {
        chatMessagesMap.remove(chatId)
    }
}

class MyFirebaseMessagingService : FirebaseMessagingService() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val chatId = remoteMessage.data["chatId"] ?: return
        val jobId = remoteMessage.data["jobId"]
        val otherUserId = remoteMessage.data["otherUserId"]
        val imageUrl = remoteMessage.data["imageUrl"]
        val title = remoteMessage.data["title"] ?: "Nuevo mensaje"
        val body = remoteMessage.data["body"] ?: "Tienes un nuevo mensaje"

        if (chatId == ChatStateManager.activeChatId) {
            Log.d("FCM", "Omitiendo notificación: chat ya activo")
            return
        }

        NotificationCache.addMessage(chatId, body)
        val messages = NotificationCache.getMessages(chatId)

        val builder = NotificationCompat.Builder(this, "chat_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(messages.last())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (imageUrl != null) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)

                val style = NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(null as Bitmap?)

                builder.setStyle(style)
            } catch (e: Exception) {
                Log.e("FCM", "Error cargando miniatura", e)
            }
        } else {
            val style = NotificationCompat.InboxStyle()
                .setSummaryText("Chat")

            for (msg in messages) {
                style.addLine(msg)
            }

            builder.setStyle(style)
        }

        // Abrir el chat exacto desde la notificación
        if (!jobId.isNullOrEmpty() && !otherUserId.isNullOrEmpty()) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("jobId", jobId)
                putExtra("otherUserId", otherUserId)
            }


            val pendingIntent = PendingIntent.getActivity(
                this,
                chatId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.setContentIntent(pendingIntent)
        }

        val notificationId = chatId.hashCode()
        NotificationManagerCompat.from(this).notify(notificationId, builder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token FCM: $token")

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId).update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "Token guardado exitosamente")
                }
                .addOnFailureListener {
                    Log.e("FCM", "Error al guardar token", it)
                }
        }
    }
}
