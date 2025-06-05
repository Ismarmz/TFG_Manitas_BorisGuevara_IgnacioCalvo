package com.example.tfg_manitas.features.chat

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.tfg_manitas.data.repository.ChatRepository
import com.example.tfg_manitas.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.TopAppBar



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    jobId: String,
    otherUserId: String,
    navController: NavHostController,
    currentUserId: String = FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
    chatRepository: ChatRepository = ChatRepository()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userName = remember { mutableStateOf("...") }

    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    var chatId by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Permiso requerido para acceder a imágenes", Toast.LENGTH_SHORT).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null && chatId != null) {
                coroutineScope.launch {
                    try {
                        val ref = FirebaseStorage.getInstance()
                            .reference
                            .child("chat_images/${UUID.randomUUID()}.jpg")

                        ref.putFile(uri).await()
                        val url = ref.downloadUrl.await().toString()

                        val imageMessage = Message(
                            senderId = currentUserId,
                            text = url,
                            type = "image"
                        )
                        chatRepository.sendMessage(chatId!!, imageMessage)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }

                }
            }
        }
    )

    BackHandler {
        navController.popBackStack()
    }

    LaunchedEffect(otherUserId) {
        val repo = UserRepository()
        val result = repo.getUserById(otherUserId)
        if (result.isSuccess) {
            userName.value = result.getOrNull()?.name ?: otherUserId
        }
    }

    if (chatId != null) {
        DisposableEffect(chatId) {
            ChatStateManager.activeChatId = chatId
            onDispose {
                ChatStateManager.activeChatId = null
            }
        }
    }

    LaunchedEffect(jobId, otherUserId) {
        val result = chatRepository.getOrCreateChat(
            jobId = jobId,
            userIds = listOf(currentUserId, otherUserId)
        )
        if (result.isSuccess) {
            val chat = result.getOrNull()!!
            chatId = chat.id
            chatRepository.listenToMessages(chat.id).collect {
                messages = it
                coroutineScope.launch {
                    chatRepository.markMessagesAsRead(chat.id, currentUserId)
                    if (it.isNotEmpty()) {
                        listState.animateScrollToItem(it.lastIndex)
                    }
                }
            }
        } else {
            Toast.makeText(context, "Error al cargar chat", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .consumeWindowInsets(WindowInsets.ime),
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Perfil",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = userName.value,
                        color = Color.White
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(0xFF2F4C5A),
                titleContentColor = Color.White
            )
        )


        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            state = listState
        ) {
            items(messages) { msg ->
                val isCurrentUser = msg.senderId == currentUserId
                val time = remember(msg.timestamp) {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
                }

                Column(
                    horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isCurrentUser)
                                        Color(0xFF2F4C5A)
                                    else
                                        Color(0xFFF4A950),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                                .widthIn(max = 280.dp)
                        ) {
                            if (msg.type == "image") {
                                AsyncImage(
                                    model = msg.text,
                                    contentDescription = "Imagen enviada",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 150.dp, max = 300.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            } else {
                                Text(
                                    text = msg.text,
                                    color = if (isCurrentUser)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    if (isCurrentUser) {
                        val statusText = when (msg.status) {
                            "read" -> "Leído"
                            "sent" -> "Enviado"
                            else -> ""
                        }

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }

        Divider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            IconButton(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                imagePickerLauncher.launch("image/*")
            }) {
                Icon(Icons.Default.Image, contentDescription = "Enviar imagen")
            }

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Escribe un mensaje...") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(20.dp),
                maxLines = 5
            )

            IconButton(
                onClick = {
                    val chat = chatId ?: return@IconButton
                    val newMessage = Message(
                        senderId = currentUserId,
                        text = messageText
                    )
                    coroutineScope.launch {
                        chatRepository.sendMessage(chat, newMessage)
                        messageText = ""
                    }
                },
                enabled = messageText.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar")
            }
        }
    }
}
