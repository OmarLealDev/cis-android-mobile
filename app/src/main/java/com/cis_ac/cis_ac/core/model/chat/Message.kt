package com.cis_ac.cis_ac.core.model.chat

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false
)
