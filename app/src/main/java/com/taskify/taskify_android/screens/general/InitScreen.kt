package com.taskify.taskify_android.screens.general

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    // Letters of the app name for animation
    val letters = stringResource(R.string.app_name)
    val positions = remember { letters.map { Animatable((0..300).random().toFloat()) } }
    val directions = remember { letters.map { (0..3).random() } }

    // Animate each letter to position 0
    letters.forEachIndexed { index, _ ->
        LaunchedEffect(Unit) {
            positions[index].animateTo(0f, animationSpec = tween(2000))
        }
    }

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
        Row {
            letters.forEachIndexed { index, letter ->
                Text(
                    text = letter.toString(),
                    fontSize = 48.sp,
                    color = Color.White,
                    modifier = Modifier.offset(
                        x = if (directions[index] % 2 == 0) positions[index].value.dp else 0.dp,
                        y = if (directions[index] % 2 == 1) positions[index].value.dp else 0.dp
                    )
                )
            }
        }
    }
}
