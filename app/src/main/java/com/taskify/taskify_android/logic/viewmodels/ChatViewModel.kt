package com.taskify.taskify_android.logic.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskify.taskify_android.data.models.chat.ConversationResponse
import com.taskify.taskify_android.data.models.chat.MessageResponse
import com.taskify.taskify_android.data.repository.AuthRepository
import com.taskify.taskify_android.data.repository.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _conversations =
        MutableStateFlow<Resource<List<ConversationResponse>>>(Resource.Loading())
    val conversations: StateFlow<Resource<List<ConversationResponse>>> = _conversations

    private val _messages = MutableStateFlow<Resource<List<MessageResponse>>>(Resource.Loading())
    val messages: StateFlow<Resource<List<MessageResponse>>> = _messages

    // Per mostrar Toasts o Snackerbars d'error sense bloquejar tota la pantalla
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchConversations() {
        viewModelScope.launch {
            _conversations.value = Resource.Loading()
            _conversations.value = repository.getConversations()
        }
    }

    fun fetchMessages(conversationId: Int) {
        viewModelScope.launch {
            _messages.value = Resource.Loading()
            val result = repository.getMessages(conversationId)
            _messages.value = result

            if (result is Resource.Success) {
                repository.markAsRead(conversationId)
            }
        }
    }


    fun navigateToChatWithUser(
        otherUserId: Long,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            // 1. Busquem si ja existeix una conversa carregada localment amb aquest usuari
            // Segons el teu JSON, 'participants' és un objecte directe, no una llista
            val existing = (conversations.value as? Resource.Success)?.data?.find { conv ->
                conv.participants.id.toLong() == otherUserId
            }

            if (existing != null) {
                // Si ja existeix, cridem l'èxit directament amb la ID del xat
                onSuccess(existing.id)
            } else {
                // 2. Si no existeix, demanem al repositori que la crei al servidor
                // El repositori ara utilitza el data class CreateConversationRequest(otherUserId)
                when (val result = repository.createConversation(otherUserId)) {
                    is Resource.Success -> {
                        fetchConversations()
                        onSuccess(result.data.id)
                    }

                    is Resource.Error -> {
                        // Passem el missatge d'error per mostrar-lo via Toast a la UI
                        onError(result.message)
                    }

                    is Resource.Loading -> {
                        // Opcional: podríem gestionar un estat de càrrega visual aquí
                    }
                }
            }
        }
    }

    fun sendMessage(conversationId: Int, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val result = repository.sendMessage(conversationId, content)
            if (result is Resource.Success) {
                // Actualitzem la llista de missatges si el GET anterior va ser exitós
                val currentList = (_messages.value as? Resource.Success)?.data ?: emptyList()
                _messages.value = Resource.Success(currentList + result.data)
            } else if (result is Resource.Error) {
                _errorMessage.value = result.message
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}