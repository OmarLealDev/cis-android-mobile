package com.cis_ac.cis_ac.ui.feature.chat.chatList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.data.auth.AuthRepository
import com.cis_ac.cis_ac.data.auth.FirebaseAuthRepository
import com.cis_ac.cis_ac.data.chat.ChatRepository
import com.cis_ac.cis_ac.data.chat.FirestoreChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val chatRepository: ChatRepository = FirestoreChatRepository(),
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        loadChatPreviews()
    }

    private fun loadChatPreviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (!currentUserId.isNullOrEmpty()) {
                    chatRepository.getChatPreviewsFlow(currentUserId).collect { chatPreviews ->
                        _uiState.update {
                            it.copy(
                                chatPreviews = chatPreviews,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Usuario no autenticado"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error cargando chats"
                    )
                }
            }
        }
    }

    fun refresh() {
        loadChatPreviews()
    }
}
