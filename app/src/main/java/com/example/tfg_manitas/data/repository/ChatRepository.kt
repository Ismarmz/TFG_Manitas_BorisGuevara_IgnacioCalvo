package com.example.tfg_manitas.data.repository

import com.example.tfg_manitas.features.chat.Chat
import com.example.tfg_manitas.features.chat.Message
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val chatsCol = db.collection("chats")

    // Crear chat si no existe
    suspend fun getOrCreateChat(jobId: String, userIds: List<String>): Result<Chat> {
        return try {
            val query = chatsCol
                .whereEqualTo("jobId", jobId)
                .whereEqualTo("userIds", userIds.sorted()) // asegurar orden
                .get()
                .await()

            if (!query.isEmpty) {
                val doc = query.documents.first()
                Result.success(doc.toObject(Chat::class.java)!!.copy(id = doc.id))
            } else {
                val doc = chatsCol.document()
                val newChat = Chat(
                    id = doc.id,
                    jobId = jobId,
                    userIds = userIds.sorted()
                )
                doc.set(newChat).await()
                Result.success(newChat)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Escuchar mensajes en tiempo real
    fun listenToMessages(chatId: String) = callbackFlow<List<Message>> {
        val listener = chatsCol
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    close(error ?: Exception("Error al escuchar mensajes"))
                    return@addSnapshotListener
                }

                val messages = snapshot.mapNotNull { it.toObject(Message::class.java).copy(id = it.id) }
                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    // Enviar mensaje
    suspend fun sendMessage(chatId: String, message: Message): Result<Unit> {
        return try {
            val msgRef = chatsCol
                .document(chatId)
                .collection("messages")
                .document()

            val newMessage = message.copy(id = msgRef.id, status = "sent")

            msgRef.set(newMessage).await()

            // actualizar resumen del chat
            chatsCol.document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to message.text,
                        "lastTimestamp" to message.timestamp
                    )
                ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenToUserChats(userId: String) = callbackFlow<List<Chat>> {
        val listener = db.collection("chats")
            .whereArrayContains("userIds", userId)
            .orderBy("lastTimestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    close(error ?: Exception("Error al escuchar chats"))
                    return@addSnapshotListener
                }

                val chats = snapshot.mapNotNull { it.toObject(Chat::class.java).copy(id = it.id) }
                trySend(chats)
            }

        awaitClose { listener.remove() }
    }

    suspend fun markMessagesAsRead(chatId: String, currentUserId: String) {
        val messagesRef = db.collection("chats").document(chatId).collection("messages")
        val unreadSnapshot = messagesRef
            .whereNotEqualTo("senderId", currentUserId)
            .whereEqualTo("status", "sent")
            .get()
            .await()

        for (doc in unreadSnapshot.documents) {
            doc.reference.update("status", "read")
        }
    }


}
