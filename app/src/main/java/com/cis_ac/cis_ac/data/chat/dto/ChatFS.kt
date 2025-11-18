package com.cis_ac.cis_ac.data.chat.dto

import com.cis_ac.cis_ac.core.model.chat.Chat

data class ChatFS(
    val id: String? = null,
    val participantIds: List<String>? = null,
    val participantNames: Map<String, String>? = null,
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long? = null,
    val lastMessageSenderId: String? = null,
    val unreadCount: Map<String, Int>? = null,
    val createdAt: Long? = null
) {
    fun toDomain(): Chat? {
        return Chat(
            id = id ?: return null,
            participantIds = participantIds ?: return null,
            participantNames = participantNames ?: emptyMap(),
            lastMessage = lastMessage ?: "",
            lastMessageTimestamp = lastMessageTimestamp ?: 0,
            lastMessageSenderId = lastMessageSenderId ?: "",
            unreadCount = unreadCount ?: emptyMap(),
            createdAt = createdAt ?: return null
        )
    }
}

fun Chat.toFirestore(): ChatFS {
    return ChatFS(
        id = id,
        participantIds = participantIds,
        participantNames = participantNames,
        lastMessage = lastMessage,
        lastMessageTimestamp = lastMessageTimestamp,
        lastMessageSenderId = lastMessageSenderId,
        unreadCount = unreadCount,
        createdAt = createdAt
    )
}
