package com.taskify.taskify_android.screens.general

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.taskify.taskify_android.logic.viewmodels.AuthViewModel

@Composable
fun HomeScreen(navController: NavController, authViewModel: AuthViewModel) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Text("Home Screen", modifier = Modifier.padding(padding))
    }
}