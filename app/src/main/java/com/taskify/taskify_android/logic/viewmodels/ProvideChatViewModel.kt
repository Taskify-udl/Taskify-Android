package com.taskify.taskify_android.logic.viewmodels

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taskify.taskify_android.data.network.RetrofitInstance
import com.taskify.taskify_android.data.repository.AuthRepository

@Composable
fun provideChatViewModel(context: Context = LocalContext.current): ChatViewModel {
    return viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    // Utilitzem el mateix mètode per obtenir l'API amb el token interceptat
                    val api = RetrofitInstance.getApi(context)
                    val repository = AuthRepository(api)
                    return ChatViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )
}