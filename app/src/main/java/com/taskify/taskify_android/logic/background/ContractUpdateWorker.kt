package com.taskify.taskify_android.logic.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.taskify.taskify_android.MainActivity
import com.taskify.taskify_android.MyApplication
import com.taskify.taskify_android.data.models.entities.ContractStatus
import com.taskify.taskify_android.data.repository.Resource

class ContractUpdateWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // 1. Accés al repositori a través de l'Application class
        val repository = (applicationContext as MyApplication).authRepository
        val sharedPrefs = applicationContext.getSharedPreferences("taskify_prefs", Context.MODE_PRIVATE)

        Log.d("ContractWorker", "Checking for updates in background...")

        // 2. Consulta a l'API dels contractes actuals
        val response = repository.getMyContracts()

        if (response is Resource.Success) {
            val contracts = response.data

            contracts.forEach { contract ->
                val lastStatus = sharedPrefs.getString("contract_status_${contract.id}", null)

                // 🚩 CAS A: EL CONTRACTE ÉS NOU (No el teníem a SharedPrefs)
                if (lastStatus == null) {
                    showNotification(
                        "New Service Request! 📩",
                        "You have a new request for '${contract.serviceName}'."
                    )
                }
                // 🚩 CAS B: L'ESTAT HA CANVIAT (Sincronització amb qualsevol canvi)
                else if (lastStatus != contract.status.name) {

                    // Personalitzem el títol segons l'estat nou
                    val title = when (contract.status) {
                        ContractStatus.ACCEPTED -> "Booking Accepted! ✅"
                        ContractStatus.REJECTED -> "Booking Rejected 🚫"
                        ContractStatus.CANCELLED -> "Booking Cancelled ❌"
                        ContractStatus.ACTIVE -> "Service Started! 🚀"
                        ContractStatus.FINISHED -> "Service Finished! 🏁"
                        else -> "Contract Update"
                    }

                    val message = "The status of '${contract.serviceName}' is now ${contract.status.name.lowercase()}."

                    showNotification(title, message)
                }

                // IMPORTANT: Actualitzem el SharedPrefs SEMPRE per evitar notificacions duplicades
                sharedPrefs.edit().putString("contract_status_${contract.id}", contract.status.name).apply()
            }
        }

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "contract_updates"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal per a Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Contract Status Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for new contracts and status changes"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Configurar l'intent perquè obri l'App en clicar
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            System.currentTimeMillis().toInt(), // ID únic per evitar conflictes
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_notify_chat) // Pots canviar-lo per la teva icona de Taskify
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        // Fem servir el temps actual com a ID perquè no se sobreescriguin si n'arriben diverses de cop
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}