package com.cis_ac.cis_ac.data.chat

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.chat.Chat
import com.cis_ac.cis_ac.core.model.chat.ChatPreview
import com.cis_ac.cis_ac.core.model.chat.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {


    suspend fun getOrCreateChat(currentUserId: String, otherUserId: String): Result<String>
    suspend fun sendMessage(chatId: String, senderId: String, senderName: String, content: String): Result<Unit>
    fun getMessagesFlow(chatId: String): Flow<List<Message>>
    fun getChatPreviewsFlow(userId: String): Flow<List<ChatPreview>>
    suspend fun markMessagesAsRead(chatId: String, userId: String): Result<Unit>
    suspend fun getChatById(chatId: String): Result<Chat>
}
