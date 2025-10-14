package com.taskify.taskify_android.screens.general


import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.taskify.taskify_android.logic.viewmodels.AuthViewModel
import com.taskify.taskify_android.ui.theme.Dark
import com.taskify.taskify_android.ui.theme.PrimaryColor
import com.taskify.taskify_android.ui.theme.TopGradientEnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController) {
    val fakeMessages = listOf(
        "Hello! How can I help you today?",
        "I need a plumber for my kitchen sink.",
        "Sure, I have a slot available tomorrow at 14:00.",
        "Perfect, thank you!"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chat", color = PrimaryColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TopGradientEnd)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Chat messages
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                fakeMessages.forEachIndexed { index, msg ->
                    val isUser = index % 2 != 0 // Naizmjenično user i provider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isUser) TopGradientEnd else Color(0xFFD1E8FF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = msg,
                                color = if (isUser) Color.White else Dark
                            )
                        }
                    }
                }
            }

            // Input field
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Type a message...", color = Dark.copy(alpha = 0.6f)) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = TopGradientEnd,
                    focusedIndicatorColor = TopGradientEnd,
                    unfocusedIndicatorColor = Dark.copy(alpha = 0.3f),
                    focusedPlaceholderColor = Dark.copy(alpha = 0.6f),
                    unfocusedPlaceholderColor = Dark.copy(alpha = 0.6f)
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}
