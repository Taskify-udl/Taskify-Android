// MyApplication.kt
package com.taskify.taskify_android

import android.app.Application
import com.taskify.taskify_android.data.network.ApiService
import com.taskify.taskify_android.data.repository.AuthRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApplication : Application() {
    // Inicialització manual simple del repositori
    val authRepository: AuthRepository by lazy {
        val api = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // La teva URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
        AuthRepository(api)
    }
}