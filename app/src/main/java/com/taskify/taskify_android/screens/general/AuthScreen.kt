package com.taskify.taskify_android.screens.general

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.taskify.taskify_android.R

@Composable
fun AuthScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Welcome to " + stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium
            )

            // Button to navigate to the Login screen
            Button(onClick = {
                navController.navigate("login") {
                    popUpTo("authScreen") { inclusive = true }
                    launchSingleTop = true
                }

            }) {
                Text("Login")
            }

            // Button to navigate to the Register screen
            Button(onClick = {
                navController.navigate("register") {
                    popUpTo("authScreen") { inclusive = true }
                    launchSingleTop = true
                }

            }) {
                Text("Register")
            }
        }
    }
}