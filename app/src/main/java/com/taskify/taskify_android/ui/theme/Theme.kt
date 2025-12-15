package com.taskify.taskify_android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = White,          // Svetla boja za glavne elemente (na tamnoj pozadini)
    secondary = Gray,         // Sekundarna svetla boja
    background = PrimaryDark, // Pozadina: Čista Crna
    surface = SecondaryColor, // Površine: Tamno Siva
    onPrimary = PrimaryDark,  // Tekst na primary: Tamno
    onSecondary = White,      // Tekst na secondary: Svetlo
    onBackground = White,     // Tekst na pozadini: Svetlo
    onSurface = LightGray,    // Tekst na površinama: Svetlo Siva
    outline = CardGray        // Linije/Borderi
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,   // Glavna crna
    secondary = SecondaryColor, // Tamno siva
    background = BgWhite,     // Pozadina: Čista Bela
    surface = BgSecondary,    // Površine: Svetlo Siva
    onPrimary = White,        // Tekst na primary: Svetlo
    onSecondary = White,
    onBackground = TextDark,  // Tekst na pozadini: Tamno
    onSurface = TextGray,     // Tekst na površinama: Siva
    outline = BorderLight
)

@Composable
fun TaskifyAndroidTheme(
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}