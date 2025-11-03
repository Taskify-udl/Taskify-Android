package com.taskify.taskify_android.screens.general

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import com.taskify.taskify_android.R
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntSize
import com.taskify.taskify_android.ui.theme.BgSecondary
import com.taskify.taskify_android.ui.theme.BgWhite
import com.taskify.taskify_android.ui.theme.BorderLight
import com.taskify.taskify_android.ui.theme.BrandBlue
import com.taskify.taskify_android.ui.theme.TextDark
import com.taskify.taskify_android.ui.theme.TextGray


@Composable
fun InitScreen(navController: NavHostController) {

    var stage by remember { mutableIntStateOf(0) }
    var sizePx by remember { mutableStateOf(IntSize(0, 0)) }

    // Animació suau del degradat
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

    // Animacions
    val alphaWelcome by animateFloatAsState(
        targetValue = if (stage == 0) 1f else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "alphaWelcome"
    )
    val offsetWelcome by animateDpAsState(
        targetValue = if (stage == 0) 0.dp else (-18).dp,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "offsetWelcome"
    )

    val alphaLogo by animateFloatAsState(
        targetValue = if (stage == 1) 1f else 0f,
        animationSpec = tween(1200, easing = LinearOutSlowInEasing),
        label = "alphaLogo"
    )
    val scaleLogo by animateFloatAsState(
        targetValue = if (stage == 1) 1f else 0.85f,
        animationSpec = tween(1200, easing = OvershootEasing),
        label = "scaleLogo"
    )

    // Seqüència
    LaunchedEffect(Unit) {
        stage = 0
        delay(2600)
        stage = -1
        delay(700)
        stage = 1
        delay(2800)
        navController.navigate("authScreen") {
            popUpTo("initScreen") { inclusive = true }
            launchSingleTop = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords -> sizePx = coords.size },
        contentAlignment = Alignment.Center
    ) {
        val widthF = sizePx.width.toFloat().coerceAtLeast(1f)
        val heightF = sizePx.height.toFloat().coerceAtLeast(1f)

        val startA = Offset(widthF * (1f - progress), 0f)
        val endA = Offset(0f, heightF * progress)

        val startB = Offset(0f, heightF * (1f - progress))
        val endB = Offset(widthF * progress, heightF)


        // Fons 1: base clara
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

        // Fons 2: lleu toc blau i moviment
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

        // Text inicial
        if (stage == 0 || (stage == -1 && alphaWelcome > 0f)) {
            Text(
                text = "Welcome to",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = TextDark.copy(alpha = alphaWelcome),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = offsetWelcome),
                style = TextStyle(
                    letterSpacing = 1.2.sp
                )
            )
        }

        // Logo
        if (stage == 1 || alphaLogo > 0f) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(160.dp)
                    .scale(scaleLogo)
                    .alpha(alphaLogo)
                    .align(Alignment.Center)
            )
        }
    }
}

/** Easing amb rebot suau */
val OvershootEasing = Easing { t ->
    val tension = 2.0f
    ((t - 1f).let { it * it * ((tension + 1f) * it + tension) + 1f })
}

