package com.cis_ac.cis_ac.data.notification

import com.cis_ac.cis_ac.core.Result
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FirestoreNotificationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance()
) : NotificationRepository {

    private val fcmTokensCollection = db.collection("fcm_tokens")

    override suspend fun saveFCMToken(userId: String, token: String): Result<Unit> {
        return try {
            fcmTokensCollection.document(userId)
                .set(mapOf("token" to token, "updatedAt" to System.currentTimeMillis()))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error saving FCM token", e)
        }
    }

    override suspend fun getFCMToken(userId: String): Result<String?> {
        return try {
            val document = fcmTokensCollection.document(userId).get().await()
            val token = document.getString("token")
            Result.Success(token)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error getting FCM token", e)
        }
    }

    override suspend fun sendMessageNotification(
        receiverUserId: String,
        senderId: String,
        senderName: String,
        messageContent: String,
        chatId: String
    ): Result<Unit> {
        return try {
            // Get receiver's FCM token
            val tokenResult = getFCMToken(receiverUserId)
            when (tokenResult) {
                is Result.Success -> {
                    val token = tokenResult.data
                    if (token != null) {

                        println("Would send notification to token: $token")
                        println("From: $senderName (ID: $senderId), Message: $messageContent, ChatId: $chatId")

                        val notificationData = mapOf(
                            "chatId" to chatId,
                            "senderId" to senderId,
                            "senderName" to senderName,
                            "message" to messageContent
                        )
                        println("Notification data: $notificationData")



                        Result.Success(Unit)
                    } else {
                        Result.Error("No FCM token found for user")
                    }
                }
                is Result.Error -> {
                    Result.Error(tokenResult.message)
                }
                is Result.Loading -> {
                    Result.Error("Still loading FCM token")
                }
                is Result.UserProfile -> {
                    Result.Error("Unexpected response type")
                }
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error sending notification")
        }
    }
}
