package com.cis_ac.cis_ac.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cis_ac.cis_ac.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            val chatId = remoteMessage.data["chatId"]
            val senderId = remoteMessage.data["senderId"]
            val senderName = remoteMessage.data["senderName"]
            val messageContent = remoteMessage.data["message"]

            val authRepository = com.cis_ac.cis_ac.data.auth.FirebaseAuthRepository()
            val currentUserId = authRepository.getCurrentUserId()

            if (senderId == currentUserId) {
                return
            }

            if (chatId != null && !ChatStateManager.shouldShowNotification(chatId)) {
                return
            }

            if (chatId != null && senderName != null && messageContent != null) {
                showNotification(senderName, messageContent, chatId)
            }
        }

        remoteMessage.notification?.let {
            val chatId = remoteMessage.data["chatId"]

            if (chatId != null && !ChatStateManager.shouldShowNotification(chatId)) {
                return
            }

            showNotification(
                it.title ?: "Nuevo mensaje",
                it.body ?: "",
                chatId
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        try {
            val authRepository = com.cis_ac.cis_ac.data.auth.FirebaseAuthRepository()
            val notificationRepository = FirestoreNotificationRepository()

            CoroutineScope(Dispatchers.IO).launch {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    notificationRepository.saveFCMToken(currentUserId, token)
                    println("FCM token saved for user: $currentUserId")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showNotification(title: String, body: String, chatId: String?) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(notificationManager)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            chatId?.let { putExtra("chatId", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mensajes de Chat",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevos mensajes en el chat"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "chat_messages"
        private const val NOTIFICATION_ID = 1
    }
}
