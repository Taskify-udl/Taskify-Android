package com.taskify.taskify_android.screens.general.chat

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.taskify.taskify_android.data.models.chat.ConversationResponse
import com.taskify.taskify_android.data.repository.Resource
import com.taskify.taskify_android.logic.viewmodels.AuthViewModel
import com.taskify.taskify_android.logic.viewmodels.ChatViewModel
import com.taskify.taskify_android.ui.theme.BgSecondary
import com.taskify.taskify_android.ui.theme.BgWhite
import com.taskify.taskify_android.ui.theme.BrandBlue
import com.taskify.taskify_android.ui.theme.Dark
import com.taskify.taskify_android.ui.theme.TextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel
) {
    val conversationsState by chatViewModel.conversations.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Càrrega inicial de converses
    LaunchedEffect(Unit) {
        chatViewModel.fetchConversations()
    }

    // 🌈 Mateixa animació de fons que la HomeScreen
    val infiniteTransition = rememberInfiniteTransition(label = "inboxBgAnim")
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
                start = Offset(widthF * (1f - progress), 0f),
                end = Offset(0f, heightF * progress)
            )
        ))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Messages", fontWeight = FontWeight.Bold, color = TextDark) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrandBlue)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (val state = conversationsState) {
                    is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = BrandBlue)
                    is Resource.Error -> Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                    is Resource.Success -> {
                        if (state.data.isEmpty()) {
                            Text("No conversations yet", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.data) { conversation ->
                                    ConversationItem(
                                        conversation = conversation,
                                        currentUserId = currentUser?.id ?: -1,
                                        onClick = {
                                            navController.navigate("chatDetail/${conversation.id}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: ConversationResponse,
    currentUserId: Long,
    onClick: () -> Unit
) {
    val otherParticipant = conversation.participants

    // 🚩 Millora de seguretat: filtrem nuls i espais sobrants
    val displayName = remember(otherParticipant) {
        val first = otherParticipant.firstName ?: ""
        val last = otherParticipant.lastName ?: ""
        val full = "$first $last".trim()
        if (full.isEmpty()) "Unknown User" else full
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar amb inicial segura
            Box(
                modifier = Modifier.size(50.dp).background(BrandBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    // Agafem la primera lletra del nom real si existeix, si no "U"
                    text = displayName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                    fontSize = 20.sp
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Dark)
                Text(
                    text = conversation.lastMessage?.content ?: "No messages yet",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Badge de missatges no llegits
            if (conversation.unreadCount > 0) {
                Box(
                    modifier = Modifier.size(24.dp).background(Color.Red, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}