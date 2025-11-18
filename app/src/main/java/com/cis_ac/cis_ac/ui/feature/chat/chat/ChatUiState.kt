package com.cis_ac.cis_ac.ui.feature.chat.chat

import com.cis_ac.cis_ac.core.model.chat.Message

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val currentMessage: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val chatId: String = "",
    val otherParticipantName: String = "",
    val currentUserId: String = ""
)
