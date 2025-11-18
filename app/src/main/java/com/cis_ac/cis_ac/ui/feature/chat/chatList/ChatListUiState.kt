package com.cis_ac.cis_ac.ui.feature.chat.chatList

import com.cis_ac.cis_ac.core.model.chat.ChatPreview

data class ChatListUiState(
    val chatPreviews: List<ChatPreview> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
