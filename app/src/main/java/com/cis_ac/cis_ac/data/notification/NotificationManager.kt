package com.cis_ac.cis_ac.data.notification

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.data.auth.AuthRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await


class NotificationManager(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) {


    suspend fun initializeForCurrentUser(): Result<Unit> {
        return try {
            val currentUserId = authRepository.getCurrentUserId()
            if (currentUserId != null) {
                val token = FirebaseMessaging.getInstance().token.await()
                notificationRepository.saveFCMToken(currentUserId, token)
                Result.Success(Unit)
            } else {
                Result.Error("No authenticated user found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to initialize notifications")
        }
    }


    suspend fun refreshToken(): Result<Unit> {
        return initializeForCurrentUser()
    }


    suspend fun sendChatNotification(
        receiverUserId: String,
        senderId: String,
        senderName: String,
        messageContent: String,
        chatId: String
    ): Result<Unit> {
        return notificationRepository.sendMessageNotification(
            receiverUserId = receiverUserId,
            senderId = senderId,
            senderName = senderName,
            messageContent = messageContent,
            chatId = chatId
        )
    }
}
