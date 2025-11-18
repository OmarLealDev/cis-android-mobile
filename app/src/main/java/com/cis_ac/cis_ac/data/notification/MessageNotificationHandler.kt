package com.cis_ac.cis_ac.data.notification

import com.cis_ac.cis_ac.core.model.chat.Message
import com.cis_ac.cis_ac.data.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged


class MessageNotificationHandler(
    private val authRepository: AuthRepository,
    private val localNotificationService: LocalNotificationService?
) {

    private var lastProcessedMessageId: String? = null


    suspend fun handleMessageNotifications(
        messagesFlow: Flow<List<Message>>,
        chatId: String
    ) {
        messagesFlow
            .distinctUntilChanged()
            .collect { messages ->
                val currentUserId = authRepository.getCurrentUserId()

                val latestMessage = messages.lastOrNull()

                if (latestMessage != null &&
                    latestMessage.senderId != currentUserId &&
                    latestMessage.id != lastProcessedMessageId &&
                    isRecentMessage(latestMessage)) {

                    localNotificationService?.showChatNotification(
                        senderName = latestMessage.senderName,
                        messageContent = latestMessage.content,
                        chatId = chatId
                    )

                    lastProcessedMessageId = latestMessage.id
                }
            }
    }


    private fun isRecentMessage(message: Message): Boolean {
        val now = System.currentTimeMillis()
        val messageAge = now - message.timestamp
        return messageAge < 10_000 // 10 seconds
    }
}
