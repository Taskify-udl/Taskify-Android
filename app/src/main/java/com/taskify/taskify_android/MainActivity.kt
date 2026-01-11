package com.taskify.taskify_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.taskify.taskify_android.screens.general.NavigationGraph
import com.taskify.taskify_android.ui.theme.TaskifyAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskifyAndroidTheme() {
                val navController = rememberNavController()
                NavigationGraph(navController = navController)
            }
        }
    }
}