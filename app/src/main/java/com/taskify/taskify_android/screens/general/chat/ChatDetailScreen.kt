package com.taskify.taskify_android.screens.general.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.taskify.taskify_android.data.models.chat.MessageResponse
import com.taskify.taskify_android.data.repository.Resource
import com.taskify.taskify_android.logic.viewmodels.AuthViewModel
import com.taskify.taskify_android.logic.viewmodels.ChatViewModel
import com.taskify.taskify_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: Int,
    navController: NavController,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel
) {
    val messagesState by chatViewModel.messages.collectAsState()
    val conversationsState by chatViewModel.conversations.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    var textState by remember { mutableStateOf("") }

    // 🆕 1. Creem l'estat de la llista per controlar el scroll
    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        chatViewModel.fetchMessages(conversationId)
    }

    // --- Lògica d'interfície (DisplayName, Background, etc.) ---
    val otherUser = remember(conversationsState) {
        (conversationsState as? Resource.Success)?.data?.find { it.id == conversationId }?.participants
    }
    val displayName = remember(otherUser) {
        val full = "${otherUser?.firstName ?: ""} ${otherUser?.lastName ?: ""}".trim()
        full.ifEmpty { "User" }
    }

    // Background animation...
    val infiniteTransition = rememberInfiniteTransition(label = "chatBgAnim")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(10000, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "progressAnim"
    )
    var sizePx by remember { mutableStateOf(IntSize(0, 0)) }

    Box(modifier = Modifier.fillMaxSize().onGloballyPositioned { sizePx = it.size }) {
        val widthF = sizePx.width.toFloat().coerceAtLeast(1f)
        val heightF = sizePx.height.toFloat().coerceAtLeast(1f)
        Box(modifier = Modifier.matchParentSize().background(
            Brush.linearGradient(
                colors = listOf(BgWhite, BgSecondary, BrandBlue.copy(alpha = 0.12f)),
                start = Offset(widthF * (1f - progress), 0f), end = Offset(0f, heightF * progress)
            )
        ))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(shadowElevation = 4.dp, color = Color.White.copy(alpha = 0.9f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrandBlue)
                        }

                        // 👤 Avatar de l'usuari a la TopBar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(BrandBlue.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Dark)
                            Text(text = "Online", fontSize = 12.sp, color = Color(0xFF4CAF50)) // Simulat
                        }
                    }
                }
            },
            bottomBar = {
                Surface(color = Color.White, shadowElevation = 12.dp) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .navigationBarsPadding()
                            .imePadding()
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textState,
                            onValueChange = { textState = it },
                            placeholder = { Text("Write a message...", fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(28.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandBlue,
                                unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor = Color(0xFFF7F8FA),
                                unfocusedContainerColor = Color(0xFFF7F8FA)
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (textState.isNotBlank()) {
                                    chatViewModel.sendMessage(conversationId, textState)
                                    textState = ""
                                    // El scroll es dispararà automàticament gràcies al LaunchedEffect de baix
                                }
                            },
                            modifier = Modifier.size(48.dp).background(BrandBlue, CircleShape)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (val state = messagesState) {
                    is Resource.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = BrandBlue)
                    is Resource.Success -> {
                        val messageList = state.data ?: emptyList()

                        // 🆕 2. Efecte automàtic: Quan la mida de la llista canvia, fem scroll al final
                        LaunchedEffect(messageList.size) {
                            if (messageList.isNotEmpty()) {
                                listState.animateScrollToItem(messageList.size - 1)
                            }
                        }

                        if (messageList.isEmpty()) {
                            EmptyChatPlaceholder()
                        } else {
                            LazyColumn(
                                // 🆕 3. Vinculem l'estat del scroll a la LazyColumn
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(messageList, key = { it.id }) { message ->
                                    ChatBubble(
                                        message = message,
                                        isMe = message.sender.toLong() == currentUser?.id
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: MessageResponse, isMe: Boolean) {
    val bubbleColor = if (isMe) BrandBlue else Color.White
    val textColor = if (isMe) Color.White else Dark
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isMe) 18.dp else 2.dp,
        bottomEnd = if (isMe) 2.dp else 18.dp
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = shape,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.content,
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
                Text(
                    text = message.timestamp.substring(11, 16),
                    fontSize = 10.sp,
                    color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyChatPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Chat, contentDescription = null, tint = BrandBlue.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("No messages yet", color = Color.Gray)
        Text("Start the conversation!", fontSize = 12.sp, color = Color.Gray.copy(alpha = 0.7f))
    }
}