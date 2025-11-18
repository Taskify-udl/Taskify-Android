package com.taskify.taskify_android.screens.general

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.taskify.taskify_android.R
import com.taskify.taskify_android.ui.theme.*

import java.util.Locale

@Composable
fun AuthScreen(navController: NavHostController) {
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

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
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
                    text = stringResource(id = R.string.welcome_taskify),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(id = R.string.connect_professionals),
                    fontSize = 16.sp,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                AnimatedButton(
                    text = stringResource(id = R.string.login_button),
                    onClick = {
                        navController.navigate("login") {
                            popUpTo("authScreen") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                AnimatedButton(
                    text = stringResource(id = R.string.register),
                    onClick = {
                        navController.navigate("register") {
                            popUpTo("authScreen") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    isOutlined = true
                )

                // 🌐 Language switcher
                LanguageSwitcher()
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

@Composable
fun LanguageSwitcher() {
    val context = LocalContext.current
    var currentLang by remember { mutableStateOf("EN") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(BrandBlue.copy(alpha = 0.8f), Color.Cyan.copy(alpha = 0.8f))
                )
            )
            .border(1.dp, BrandBlue, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp)
            .clickable {
                // ciklično mijenjamo jezik
                currentLang = when (currentLang) {
                    "EN" -> "ES"
                    "ES" -> "CA"
                    else -> "EN"
                }

                // Promjena jezika u aplikaciji
                setAppLocale(context, currentLang.lowercase())

            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🌐 Language: $currentLang",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

// Jedina funkcija za promjenu jezika
fun setAppLocale(context: Context, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = Configuration(context.resources.configuration)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    } else {
        config.locale = locale
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}

