package com.taskify.taskify_android.screens.general

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taskify.taskify_android.logic.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(authViewModel: AuthViewModel = viewModel()) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Register Screen", style = MaterialTheme.typography.headlineMedium)
    }
}