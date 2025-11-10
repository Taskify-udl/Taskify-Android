package com.taskify.taskify_android.screens.general

import android.graphics.RenderEffect
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.taskify.taskify_android.ui.theme.BgSecondary
import com.taskify.taskify_android.ui.theme.BgWhite
import com.taskify.taskify_android.ui.theme.BorderLight
import com.taskify.taskify_android.ui.theme.BrandBlue
import com.taskify.taskify_android.ui.theme.CardGray
import com.taskify.taskify_android.ui.theme.TextDark
import com.taskify.taskify_android.ui.theme.TextGray

@Composable
fun AuthScreen(navController: NavHostController) {
    // 🔁 Animació del fons diagonal
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

    var sizePx by remember { mutableStateOf(IntSize(0, 0)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords -> sizePx = coords.size }
    ) {
        val widthF = sizePx.width.toFloat().coerceAtLeast(1f)
        val heightF = sizePx.height.toFloat().coerceAtLeast(1f)

        val startA = Offset(widthF * (1f - progress), 0f)
        val endA = Offset(0f, heightF * progress)

        val startB = Offset(0f, heightF * (1f - progress))
        val endB = Offset(widthF * progress, heightF)

        // 🌈 Fons amb doble degradat diagonal
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

        // 💠 Card translúcida amb efecte blur (Glassmorphism)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coords -> sizePx = coords.size }
        ) {
            val widthF = sizePx.width.toFloat().coerceAtLeast(1f)
            val heightF = sizePx.height.toFloat().coerceAtLeast(1f)

            val startA = Offset(widthF * (1f - progress), 0f)
            val endA = Offset(0f, heightF * progress)
            val startB = Offset(0f, heightF * (1f - progress))
            val endB = Offset(widthF * progress, heightF)

            // 🌈 Fons doble degradat
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.linearGradient(
                            listOf(BgWhite, BgSecondary, BrandBlue.copy(alpha = 0.12f)),
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
                            listOf(BrandBlue.copy(alpha = 0.25f), TextGray.copy(alpha = 0.15f)),
                            start = startB,
                            end = endB
                        )
                    )
            )

            // 💠 Card centrada amb glassmorphism
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center // 🔑 centra la card
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 28.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Welcome to Taskify",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Connect with the best professionals",
                        fontSize = 16.sp,
                        color = Color(0xFF333333),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    AnimatedButton(
                        text = "Log In",
                        onClick = { navController.navigate("login") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )

                    AnimatedButton(
                        text = "Register",
                        onClick = { navController.navigate("register") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        isOutlined = true
                    )

                }
            }
        }
    }
}

@Composable
fun AnimatedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isOutlined: Boolean = false
) {
    // animació d’escala
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (pressed) 0.95f else 1f)

    val bgColor = if (isOutlined) Color.Transparent else BrandBlue
    val contentColor = if (isOutlined) BrandBlue else Color.White

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        if (isOutlined) {
            OutlinedButton(
                onClick = {
                    pressed = true
                    onClick()
                },
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(14.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(BrandBlue, Color.White))
                ),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
            ) {
                Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        } else {
            Button(
                onClick = {
                    pressed = true
                    onClick()
                },
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = bgColor,
                    contentColor = contentColor
                )
            ) {
                Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}