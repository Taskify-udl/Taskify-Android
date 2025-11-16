package com.taskify.taskify_android.screens.general

import com.taskify.taskify_android.logic.viewmodels.AuthViewModel
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.taskify.taskify_android.R
import com.taskify.taskify_android.data.repository.Resource
import com.taskify.taskify_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInfoScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current

    val user by authViewModel.currentUser.collectAsState()
    val profileState by authViewModel.profileState.collectAsState()

    // ⏳ Load profile when screen opens
    LaunchedEffect(Unit) {
        authViewModel.loadProfile()
    }

    // Background Animation
    val infiniteTransition = rememberInfiniteTransition(label = "homeBgAnim")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progressAnim"
    )

    var sizePx by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { sizePx = it.size }
    ) {
        val widthF = sizePx.width.toFloat().coerceAtLeast(1f)
        val heightF = sizePx.height.toFloat().coerceAtLeast(1f)
        val start = Offset(widthF * (1f - progress), 0f)
        val end = Offset(0f, heightF * progress)

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            BgWhite,
                            BgSecondary,
                            BrandBlue.copy(alpha = 0.12f)
                        ),
                        start = start,
                        end = end
                    )
                )
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Profile Info",
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = BrandBlue
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->

            when (profileState) {

                is Resource.Loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = BrandBlue)
                    }
                }

                is Resource.Error -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Error loading profile", color = Color.Red)
                    }
                }

                is Resource.Success -> {
                    val scroll = rememberScrollState()

                    // Editable values
                    var name by remember { mutableStateOf(user?.fullName ?: "") }
                    var email by remember { mutableStateOf(user?.email ?: "") }
                    var username by remember { mutableStateOf(user?.username ?: "") }
                    var phone by remember { mutableStateOf(user?.phoneNumber ?: "") }

                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .verticalScroll(scroll)
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // Profile Picture
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(2.dp, BrandBlue.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.profilepic),
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        // Editable Fields
                        ProfileEditableField("Full Name", name) { name = it }
                        Spacer(Modifier.height(12.dp))
                        ProfileEditableField("Username", username) { username = it }
                        Spacer(Modifier.height(12.dp))
                        ProfileEditableField("Email", email) { email = it }
                        Spacer(Modifier.height(12.dp))
                        ProfileEditableField("Phone Number", phone) { phone = it }

                        Spacer(Modifier.height(32.dp))

                        // Save Button
                        Button(
                            onClick = {
                                val updates = mapOf(
                                    "fullName" to name,
                                    "username" to username,
                                    "email" to email,
                                    "phoneNumber" to phone
                                )

                                authViewModel.updateProfile(
                                    context = context,
                                    updates = updates,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "Profile updated!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.popBackStack()
                                    },
                                    onError = {
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("Save Changes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileEditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextGray) },
        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = BrandBlue,
            unfocusedIndicatorColor = Color.White.copy(alpha = 0.25f),
            cursorColor = BrandBlue
        )
    )
}
