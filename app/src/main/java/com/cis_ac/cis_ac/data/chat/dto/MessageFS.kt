package com.cis_ac.cis_ac.data.chat.dto

import com.cis_ac.cis_ac.core.model.chat.Message

data class MessageFS(
    val id: String? = null,
    val chatId: String? = null,
    val senderId: String? = null,
    val senderName: String? = null,
    val content: String? = null,
    val timestamp: Long? = null,
    val isRead: Boolean? = null
) {
    fun toDomain(): Message? {
        return Message(
            id = id ?: return null,
            chatId = chatId ?: return null,
            senderId = senderId ?: return null,
            senderName = senderName ?: return null,
            content = content ?: return null,
            timestamp = timestamp ?: return null,
            isRead = isRead ?: false
        )
    }
}

fun Message.toFirestore(): MessageFS {
    return MessageFS(
        id = id,
        chatId = chatId,
        senderId = senderId,
        senderName = senderName,
        content = content,
        timestamp = timestamp,
        isRead = isRead
    )
}
