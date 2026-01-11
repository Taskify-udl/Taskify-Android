package com.taskify.taskify_android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.taskify.taskify_android.logic.background.ContractUpdateWorker
import com.taskify.taskify_android.screens.general.NavigationGraph
import com.taskify.taskify_android.ui.theme.TaskifyAndroidTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configurem el Worker (Polling) per revisar contractes cada 15 min
        setupContractWorker()

        setContent {
            TaskifyAndroidTheme {
                // Estat per mostrar el diàleg d'error si no s'accepta el permís
                var showPermissionDialog by remember { mutableStateOf(false) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Si l'usuari no l'accepta, activem el diàleg informatiu
                    if (!isGranted) {
                        showPermissionDialog = true
                    }
                }

                // Demanem el permís només si estem a Android 13 o superior
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // Diàleg informatiu en cas de denegació
                if (showPermissionDialog) {
                    AlertDialog(
                        onDismissRequest = { showPermissionDialog = false },
                        title = { Text("Notifications Disabled") },
                        text = { Text("To receive updates about your contract status changes, please enable notifications in the system settings.") },
                        confirmButton = {
                            TextButton(onClick = { showPermissionDialog = false }) {
                                Text("I understand")
                            }
                        }
                    )
                }

                val navController = rememberNavController()
                NavigationGraph(navController = navController)
            }
        }
    }

    private fun setupContractWorker() {
        val workRequest = PeriodicWorkRequestBuilder<ContractUpdateWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ContractWatcher",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}