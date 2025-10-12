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
fun provideAuthViewModel(context: Context = LocalContext.current): AuthViewModel {
    return viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    val api = RetrofitInstance.getApi(context)
                    val repository = AuthRepository(api)
                    return AuthViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )
}