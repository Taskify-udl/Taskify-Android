package com.taskify.taskify_android.screens.general

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import com.taskify.taskify_android.R
import com.taskify.taskify_android.ui.theme.TopGradientEnd
import com.taskify.taskify_android.ui.theme.TopGradientStart


@Composable
fun InitScreen(navController: NavHostController) {

    // Delay before navigating (3 seconds)
    LaunchedEffect(Unit) {
        delay(3000)
        navController.navigate("authScreen") {
            popUpTo("initScreen") { inclusive = true }
            launchSingleTop = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(TopGradientStart, TopGradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Display the logo from drawable
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(150.dp)
        )
    }
}

