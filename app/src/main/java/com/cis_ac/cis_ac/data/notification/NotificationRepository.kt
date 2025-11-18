package com.cis_ac.cis_ac.data.notification

import com.cis_ac.cis_ac.core.Result

interface NotificationRepository {
    suspend fun saveFCMToken(userId: String, token: String): Result<Unit>
    suspend fun sendMessageNotification(
        receiverUserId: String,
        senderId: String,
        senderName: String,
        messageContent: String,
        chatId: String
    ): Result<Unit>
    suspend fun getFCMToken(userId: String): Result<String?>
}
