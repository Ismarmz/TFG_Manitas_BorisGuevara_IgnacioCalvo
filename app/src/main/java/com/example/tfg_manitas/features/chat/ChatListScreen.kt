package com.example.tfg_manitas.features.chat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F0))
            .padding(16.dp)
    )
    {
        Text(
            text = "Mis chats",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = Color(0xFF2F4C5A),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (chats.isEmpty()) {
            Text("No tienes chats activos.")
        } else {
            LazyColumn {
                items(chats) { chat ->
                    val otherUserId =
                        chat.userIds.firstOrNull { it != currentUserId } ?: return@items

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
                            .padding(vertical = 6.dp)
                            .clickable {
                                navController.navigate("chat/${chat.jobId}/$otherUserId")
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE6F6)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Foto de perfil",
                                tint = Color(0xFF2F4C5A),
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(end = 12.dp)
                            )

                            Column {
                                Text(
                                    text = jobTitle.value,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2F4C5A)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Último mensaje: ${chat.lastMessage}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2F4C5A).copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
