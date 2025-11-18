package com.cis_ac.cis_ac.core.model.chat

data class Chat(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0,
    val lastMessageSenderId: String = "",
    val unreadCount: Map<String, Int> = emptyMap(),
    val createdAt: Long = 0
)

data class ChatPreview(
    val chatId: String,
    val otherParticipantId: String,
    val otherParticipantName: String,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val unreadCount: Int,
    val isLastMessageFromMe: Boolean
)
