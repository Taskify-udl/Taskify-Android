package com.taskify.taskify_android.screens.general

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.taskify.taskify_android.logic.viewmodels.provideAuthViewModel
import com.taskify.taskify_android.logic.viewmodels.provideChatViewModel
import com.taskify.taskify_android.screens.general.authentication.AuthScreen
import com.taskify.taskify_android.screens.general.authentication.LoginScreen
import com.taskify.taskify_android.screens.general.authentication.RegisterScreen
import com.taskify.taskify_android.screens.general.chat.ChatDetailScreen
import com.taskify.taskify_android.screens.general.chat.InboxScreen
import com.taskify.taskify_android.screens.general.homescreen.BookingsScreen
import com.taskify.taskify_android.screens.general.homescreen.SecurityScreen


@Composable
fun NavigationGraph(navController: NavHostController) {
    val authViewModel = provideAuthViewModel()
    val chatViewModel = provideChatViewModel()

    NavHost(navController = navController, startDestination = "initScreen") {
        //  Init screen with animated app name
        composable("initScreen") {
            InitScreen(navController = navController, authViewModel)
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
            HomeScreen(navController, authViewModel, chatViewModel)
        }

        composable("profileInfoScreen") {
            ProfileInfoScreen(navController, authViewModel)
        }

        // 🔒 Security Screen
        composable("securityScreen") { // ⬅️ NOVI SCREEN
            SecurityScreen(navController, authViewModel)
        }

        //Dashboard Screen
        composable("dashboardScreen") {
            DashboardScreen(navController, authViewModel)
        }

        composable("category/{categoryName}") { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "Unknown"
            CategoryScreen(
                categoryName = categoryName,
                navController = navController
            )
        }

        composable("offerDetail/{offerName}") { backStackEntry ->
            val offerName = backStackEntry.arguments?.getString("offerName") ?: "Unknown"
            BookingsScreen(offerName = offerName, navController = navController)
        }

        composable("providerProfile/{providerName}") { backStackEntry ->
            val providerName = backStackEntry.arguments?.getString("providerName") ?: "Unknown"
            ProviderProfileScreen(
                providerName = providerName,
                navController = navController
            )
        }

        // Pantalla de la llista de xats (Safata d'entrada)
        composable("inbox") {
            InboxScreen(navController, authViewModel, chatViewModel)
        }

        // Pantalla de conversa individual
        composable("chatDetail/{conversationId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("conversationId")?.toIntOrNull() ?: -1
            ChatDetailScreen(id, navController, authViewModel, chatViewModel)
        }
    }
}
