package com.cis_ac.cis_ac.ui.feature.chat.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.data.auth.AuthRepository
import com.cis_ac.cis_ac.data.auth.FirebaseAuthRepository
import com.cis_ac.cis_ac.data.chat.ChatRepository
import com.cis_ac.cis_ac.data.chat.FirestoreChatRepository
import com.cis_ac.cis_ac.data.userprofile.UserProfileRepository
import com.cis_ac.cis_ac.data.userprofile.FirestoreUserProfileRepository
import com.cis_ac.cis_ac.data.notification.NotificationRepository
import com.cis_ac.cis_ac.data.notification.FirestoreNotificationRepository
import com.cis_ac.cis_ac.data.notification.MessageNotificationHandler
import com.cis_ac.cis_ac.data.notification.LocalNotificationService
import com.cis_ac.cis_ac.data.notification.NotificationContextHolder
import com.cis_ac.cis_ac.ui.feature.chat.utils.getUserDisplayName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(),
    private val userProfileRepository: UserProfileRepository = FirestoreUserProfileRepository(),
    private val notificationRepository: NotificationRepository = FirestoreNotificationRepository()
) : ViewModel() {

    private val chatRepository: ChatRepository = FirestoreChatRepository(
        notificationRepository = notificationRepository,
        authRepository = authRepository
    )

    private val messageNotificationHandler = MessageNotificationHandler(
        authRepository = authRepository,
        localNotificationService = NotificationContextHolder.getContext()?.let {
            LocalNotificationService(it)
        }
    )

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun initializeChat(otherUserId: String, otherUserName: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Usuario no autenticado"
                        )
                    }
                    return@launch
                }

                when (val chatResult = chatRepository.getOrCreateChat(currentUserId, otherUserId)) {
                    is Result.Success -> {
                        val chatId = chatResult.data

                        val participantName: String = otherUserName
                            ?: getUserDisplayName(otherUserId, userProfileRepository)

                        _uiState.update {
                            it.copy(
                                chatId = chatId,
                                currentUserId = currentUserId,
                                otherParticipantName = participantName,
                                isLoading = false
                            )
                        }

                        chatRepository.markMessagesAsRead(chatId, currentUserId)

                        val messagesFlow = chatRepository.getMessagesFlow(chatId)

                        viewModelScope.launch {
                            messageNotificationHandler.handleMessageNotifications(messagesFlow, chatId)
                        }

                        messagesFlow.collect { messages ->
                            _uiState.update { it.copy(messages = messages) }
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = chatResult.message
                            )
                        }
                    }
                    is Result.Loading -> {
                    }
                    is Result.UserProfile -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Respuesta inesperada del servidor"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inicializando chat"
                    )
                }
            }
        }
    }

    fun onMessageChange(message: String) {
        _uiState.update { it.copy(currentMessage = message) }
    }

    fun sendMessage() {
        val currentState = _uiState.value
        if (currentState.currentMessage.isBlank() || currentState.isSending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }

            try {
                val currentUserName = getUserDisplayName(currentState.currentUserId, userProfileRepository)

                when (val result = chatRepository.sendMessage(
                    chatId = currentState.chatId,
                    senderId = currentState.currentUserId,
                    senderName = currentUserName,
                    content = currentState.currentMessage
                )) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                currentMessage = "",
                                isSending = false,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isSending = false,
                                error = result.message
                            )
                        }
                    }
                    is Result.Loading -> {
                    }
                    is Result.UserProfile -> {
                        _uiState.update {
                            it.copy(
                                isSending = false,
                                error = "Respuesta inesperada del servidor"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSending = false,
                        error = e.message ?: "Error enviando mensaje"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
