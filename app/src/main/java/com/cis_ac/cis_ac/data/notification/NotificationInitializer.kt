package com.cis_ac.cis_ac.data.notification

import com.cis_ac.cis_ac.data.auth.AuthRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class NotificationInitializer(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) {

    suspend fun initializeNotifications() {
        try {
            val currentUserId = authRepository.getCurrentUserId()
            if (currentUserId != null) {
                val token = FirebaseMessaging.getInstance().token.await()
                notificationRepository.saveFCMToken(currentUserId, token)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
