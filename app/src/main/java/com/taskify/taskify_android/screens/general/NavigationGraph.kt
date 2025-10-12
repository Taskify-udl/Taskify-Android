package com.taskify.taskify_android.screens.general

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.taskify.taskify_android.logic.viewmodels.provideAuthViewModel


@Composable
fun NavigationGraph(navController: NavHostController) {
    val authViewModel = provideAuthViewModel()

    NavHost(navController = navController, startDestination = "initScreen") {
        //  Init screen with animated app name
        composable("initScreen") {
            InitScreen(navController = navController)
        }

        // Authentication screen where user can choose to Login or Register
        composable("authScreen") {
            AuthScreen(navController = navController)
        }

        // Placeholder Login screen
        composable("login") {
            LoginScreen(navController, authViewModel)
        }

        // Placeholder Register screen
        composable("register") {
            RegisterScreen(navController, authViewModel)
        }

        // Placeholder Home screen
        composable("homeScreen") {
            HomeScreen(navController, authViewModel)
        }
    }
}
