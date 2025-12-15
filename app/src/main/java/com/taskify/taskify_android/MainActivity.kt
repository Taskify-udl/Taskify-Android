package com.taskify.taskify_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue // ⬅️ NOVI IMPORT
import androidx.compose.runtime.remember // ⬅️ NOVI IMPORT
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.taskify.taskify_android.screens.general.NavigationGraph
import com.taskify.taskify_android.ui.theme.TaskifyAndroidTheme
import com.taskify.taskify_android.ui.theme.ThemeState // ⬅️ NOVI IMPORT (Za Dark Mode stanje)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by remember { ThemeState.isDarkTheme }

            TaskifyAndroidTheme(darkTheme = isDark) { // ⬅️ PROSLEDJUJEMO TRENUTNO STANJE TEME
                // NavigationGraph
                val navController = rememberNavController()
                NavigationGraph(navController = navController)
            }
        }

        // 🔹 Aktiviraj fullscreen immersive mod
        hideSystemUI()
    }

    private fun hideSystemUI() {
        // Ovo isključuje automatsko dodavanje paddinga za sistemske trake
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 🔹 Kontrola sistemskih traka
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        // 🔹 Sakrij status i navigacione trake
        controller.hide(WindowInsetsCompat.Type.systemBars())

        // 🔹 Omogući da se vrate prevlačenjem prema gore (gesture)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}