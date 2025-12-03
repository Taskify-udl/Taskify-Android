package com.taskify.taskify_android.screens.general

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taskify.taskify_android.logic.viewmodels.AuthViewModel
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.taskify.taskify_android.logic.validateLogin
import com.taskify.taskify_android.ui.theme.BgSecondary
import com.taskify.taskify_android.ui.theme.BgWhite
import com.taskify.taskify_android.ui.theme.BrandBlue
import com.taskify.taskify_android.ui.theme.TextDark
import com.taskify.taskify_android.ui.theme.TextGray
import androidx.compose.ui.res.stringResource
import com.taskify.taskify_android.R

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val loginState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // FIX 1: Reiniciem l'estat quan la pantalla es carrega
    LaunchedEffect(Unit) {
        authViewModel.resetState()
    }

    // FIX 2: Aquest LaunchedEffect ara capturarà el canvi de false a true
    LaunchedEffect(loginState.isSuccess) {
        if (loginState.isSuccess) {
            navController.navigate("homeScreen") {
                popUpTo("login") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bgAnim")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progressAnim"
    )

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val startA = Offset(screenWidthPx * (1f - progress), 0f)
            val endA = Offset(0f, screenHeightPx * progress)
            val startB = Offset(0f, screenHeightPx * (1f - progress))
            val endB = Offset(screenWidthPx * progress, screenHeightPx)

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                BgWhite,
                                BgSecondary,
                                BrandBlue.copy(alpha = 0.12f)
                            ),
                            start = startA,
                            end = endA
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                BrandBlue.copy(alpha = 0.25f),
                                TextGray.copy(alpha = 0.15f)
                            ),
                            start = startB,
                            end = endB
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .align(Alignment.Center)
                    .background(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 28.dp, vertical = 36.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.welcome_title),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.welcome_subtitle),
                        fontSize = 16.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = {
                            Text(
                                stringResource(R.string.username_label),
                                color = Color.Black
                            )
                        },
                        placeholder = {
                            Text(
                                stringResource(R.string.username_placeholder),
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = {
                            Text(
                                stringResource(R.string.password_label),
                                color = Color.Black
                            )
                        },
                        placeholder = {
                            Text(
                                stringResource(R.string.password_placeholder),
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        visualTransformation =
                            if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon =
                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = stringResource(R.string.password_visibility_icon),
                                    tint = Color.Black
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextButton(onClick = { }) {
                        Text(
                            text = stringResource(R.string.forgot_password),
                            style = MaterialTheme.typography.labelSmall,
                            textDecoration = TextDecoration.Underline,
                            color = TextGray
                        )
                    }

                    AnimatedButton(
                        text = stringResource(R.string.login_button),
                        onClick = {
                            localError = ""
                            val validationError = validateLogin(username, password)
                            if (validationError.isNotEmpty()) {
                                localError = validationError
                            } else {
                                authViewModel.login(username, password, context)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )

                    val errorToShow = localError.ifEmpty { loginState.error }
                    errorToShow?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.no_account),
                            fontSize = 14.sp,
                            color = TextGray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.register_button),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue,
                            modifier = Modifier.clickable {
                                navController.navigate("register") {
                                    popUpTo("login") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    if (loginState.isLoading) {
                        Spacer(Modifier.height(8.dp))
                        CircularProgressIndicator()
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = configuration.screenHeightDp.dp * 0.38f)
                    .width(configuration.screenWidthDp.dp * 0.22f)
                    .height(configuration.screenHeightDp.dp * 0.07f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        navController.navigate("authScreen") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.go_back),
                    tint = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}


@Composable
fun PasswordField(password: String, onPasswordChange: (String) -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(stringResource(id = R.string.password)) },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image =
                if (passwordVisible) Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = image,
                    contentDescription =
                        if (passwordVisible)
                            stringResource(id = R.string.hide_password)
                        else
                            stringResource(id = R.string.show_password)
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}


