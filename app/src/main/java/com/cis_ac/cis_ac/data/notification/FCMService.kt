package com.cis_ac.cis_ac.data.notification

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FCMService(
    private val notificationRepository: NotificationRepository
) {

    suspend fun initializeFCM(userId: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            notificationRepository.saveFCMToken(userId, token)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun refreshToken(userId: String) {
        initializeFCM(userId)
    }
}
