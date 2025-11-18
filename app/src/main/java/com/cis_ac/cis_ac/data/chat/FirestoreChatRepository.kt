package com.cis_ac.cis_ac.data.chat

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.chat.Chat
import com.cis_ac.cis_ac.core.model.chat.ChatPreview
import com.cis_ac.cis_ac.core.model.chat.Message
import com.cis_ac.cis_ac.data.chat.dto.ChatFS
import com.cis_ac.cis_ac.data.chat.dto.MessageFS
import com.cis_ac.cis_ac.data.chat.dto.toFirestore
import com.cis_ac.cis_ac.data.userprofile.dto.PatientFS
import com.cis_ac.cis_ac.data.userprofile.dto.ProfessionalFS
import com.cis_ac.cis_ac.data.userprofile.dto.AdminFS
import com.cis_ac.cis_ac.data.notification.NotificationRepository
import com.cis_ac.cis_ac.data.notification.LocalNotificationService
import com.cis_ac.cis_ac.data.notification.NotificationContextHolder
import com.cis_ac.cis_ac.data.auth.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.UUID

class FirestoreChatRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val notificationRepository: NotificationRepository? = null,
    private val authRepository: AuthRepository? = null
) : ChatRepository {

    private val chats = db.collection("chats")
    private val messages = db.collection("messages")
    private val localNotificationService = NotificationContextHolder.getContext()?.let {
        LocalNotificationService(it)
    }

    override suspend fun getOrCreateChat(currentUserId: String, otherUserId: String): Result<String> {
        return try {
            val sortedIds = listOf(currentUserId, otherUserId).sorted()
            val chatId = "${sortedIds[0]}_${sortedIds[1]}"

            val chatDoc = chats.document(chatId)
            val chatSnapshot = chatDoc.get().await()

            if (!chatSnapshot.exists()) {
                val newChat = Chat(
                    id = chatId,
                    participantIds = listOf(currentUserId, otherUserId),
                    participantNames = emptyMap(),
                    lastMessage = "",
                    lastMessageTimestamp = 0,
                    lastMessageSenderId = "",
                    unreadCount = mapOf(currentUserId to 0, otherUserId to 0),
                    createdAt = System.currentTimeMillis()
                )

                chatDoc.set(newChat.toFirestore()).await()
            }

            Result.Success(chatId)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error creating/getting chat", e)
        }
    }

    override suspend fun sendMessage(chatId: String, senderId: String, senderName: String, content: String): Result<Unit> {
        return try {
            val messageId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()

            val message = Message(
                id = messageId,
                chatId = chatId,
                senderId = senderId,
                senderName = senderName,
                content = content,
                timestamp = timestamp,
                isRead = false
            )

            messages.document(messageId).set(message.toFirestore()).await()

            val chatDoc = chats.document(chatId)
            val chatSnapshot = chatDoc.get().await()

            var receiverUserId: String? = null

            if (chatSnapshot.exists()) {
                val chat = chatSnapshot.toObject<ChatFS>()?.toDomain()
                if (chat != null) {
                    val updatedUnreadCount = chat.unreadCount.toMutableMap()
                    chat.participantIds.forEach { participantId ->
                        if (participantId != senderId) {
                            updatedUnreadCount[participantId] = (updatedUnreadCount[participantId] ?: 0) + 1
                            receiverUserId = participantId
                        }
                    }

                    val updatedNames = chat.participantNames.toMutableMap()
                    updatedNames[senderId] = senderName

                    val updatedChat = chat.copy(
                        lastMessage = content,
                        lastMessageTimestamp = timestamp,
                        lastMessageSenderId = senderId,
                        unreadCount = updatedUnreadCount,
                        participantNames = updatedNames
                    )

                    chatDoc.set(updatedChat.toFirestore()).await()
                }
            }

            receiverUserId?.let { receiverId ->
                notificationRepository?.sendMessageNotification(
                    receiverUserId = receiverId,
                    senderId = senderId,
                    senderName = senderName,
                    messageContent = content,
                    chatId = chatId
                )

            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error sending message", e)
        }
    }

    override fun getMessagesFlow(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = messages
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messageList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<MessageFS>()?.toDomain()
                } ?: emptyList()

                trySend(messageList)
            }

        awaitClose { listener.remove() }
    }

    override fun getChatPreviewsFlow(userId: String): Flow<List<ChatPreview>> = callbackFlow {
        val listener = chats
            .whereArrayContains("participantIds", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val chatPreviews = snapshot?.documents?.mapNotNull { doc ->
                    val chat = doc.toObject<ChatFS>()?.toDomain()
                    chat?.let { chatData ->
                        val otherParticipantId = chatData.participantIds.firstOrNull { it != userId }
                        if (otherParticipantId != null) {
                            val otherParticipantName = chatData.participantNames[otherParticipantId]
                                ?: "Usuario"

                            ChatPreview(
                                chatId = chatData.id,
                                otherParticipantId = otherParticipantId,
                                otherParticipantName = otherParticipantName,
                                lastMessage = chatData.lastMessage,
                                lastMessageTimestamp = chatData.lastMessageTimestamp,
                                unreadCount = chatData.unreadCount[userId] ?: 0,
                                isLastMessageFromMe = chatData.lastMessageSenderId == userId
                            )
                        } else null
                    }
                } ?: emptyList()

                if (chatPreviews.any { it.otherParticipantName == "Usuario" }) {
                    CoroutineScope(Dispatchers.IO).launch {
                        updateMissingParticipantNames(chatPreviews.filter { it.otherParticipantName == "Usuario" })
                    }
                }

                trySend(chatPreviews)
            }

        awaitClose { listener.remove() }
    }

    private suspend fun updateMissingParticipantNames(chatPreviewsWithMissingNames: List<ChatPreview>) {
        chatPreviewsWithMissingNames.forEach { chatPreview ->
            val userName = fetchUserName(chatPreview.otherParticipantId)
            if (userName != null) {
                try {
                    val chatDoc = chats.document(chatPreview.chatId)
                    val chatSnapshot = chatDoc.get().await()

                    if (chatSnapshot.exists()) {
                        val chat = chatSnapshot.toObject<ChatFS>()?.toDomain()
                        if (chat != null) {
                            val updatedNames = chat.participantNames.toMutableMap()
                            updatedNames[chatPreview.otherParticipantId] = userName

                            val updatedChat = chat.copy(participantNames = updatedNames)
                            chatDoc.set(updatedChat.toFirestore()).await()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun fetchUserName(userId: String): String? {
        return try {
            val patientDoc = db.collection("patients").document(userId).get().await()
            if (patientDoc.exists()) {
                return patientDoc.toObject<PatientFS>()?.fullName
            }

            val professionalDoc = db.collection("professionals").document(userId).get().await()
            if (professionalDoc.exists()) {
                return professionalDoc.toObject<ProfessionalFS>()?.fullName
            }

            val adminDoc = db.collection("admins").document(userId).get().await()
            if (adminDoc.exists()) {
                return adminDoc.toObject<AdminFS>()?.fullName
            }

            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun markMessagesAsRead(chatId: String, userId: String): Result<Unit> {
        return try {
            val chatDoc = chats.document(chatId)
            val chatSnapshot = chatDoc.get().await()

            if (chatSnapshot.exists()) {
                val chat = chatSnapshot.toObject<ChatFS>()?.toDomain()
                if (chat != null) {
                    val updatedUnreadCount = chat.unreadCount.toMutableMap()
                    updatedUnreadCount[userId] = 0

                    val updatedChat = chat.copy(unreadCount = updatedUnreadCount)
                    chatDoc.set(updatedChat.toFirestore()).await()
                }
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error marking messages as read", e)
        }
    }

    override suspend fun getChatById(chatId: String): Result<Chat> {
        return try {
            val snapshot = chats.document(chatId).get().await()
            if (!snapshot.exists()) {
                Result.Error("Chat not found")
            } else {
                val chat = snapshot.toObject<ChatFS>()?.toDomain()
                if (chat != null) {
                    Result.Success(chat)
                } else {
                    Result.Error("Invalid chat data")
                }
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error getting chat", e)
        }
    }
}
