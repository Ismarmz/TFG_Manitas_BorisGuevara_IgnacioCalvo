package com.example.tfg_manitas.features.chat

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfg_manitas.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
@Composable
fun ChatListScreen(
    navController: NavHostController,
    chatRepo: ChatRepository = ChatRepository()
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val context = LocalContext.current
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }

    LaunchedEffect(currentUserId) {
        chatRepo.listenToUserChats(currentUserId).collect {
            chats = it
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mis chats", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (chats.isEmpty()) {
            Text("No tienes chats activos.")
        } else {
            LazyColumn {
                items(chats) { chat ->
                    val otherUserId = chat.userIds.firstOrNull { it != currentUserId } ?: return@items

                    val jobTitle = remember { mutableStateOf("Cargando...") }

                    LaunchedEffect(chat.jobId) {
                        try {
                            val jobSnapshot = FirebaseFirestore.getInstance()
                                .collection("jobs")
                                .document(chat.jobId)
                                .get()
                                .await()

                            jobTitle.value = jobSnapshot.getString("title") ?: chat.jobId
                        } catch (e: Exception) {
                            jobTitle.value = "(Error al cargar título)"
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                navController.navigate("chat/${chat.jobId}/$otherUserId")
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Trabajo: ${jobTitle.value}", style = MaterialTheme.typography.titleMedium)
                            Text("Último mensaje: ${chat.lastMessage}")
                        }
                    }
                }

            }
        }
    }
}
