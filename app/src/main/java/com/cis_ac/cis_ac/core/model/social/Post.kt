package com.cis_ac.cis_ac.core.model.social

import androidx.compose.runtime.Immutable
import com.google.firebase.Timestamp

@Immutable
data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String? = null,
    val authorRole: String = "PROFESSIONAL",
    val text: String = "",
    val imageUrl: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val likeCount: Long = 0,
    val commentCount: Long = 0,
    // UI helpers
    val likedByMe: Boolean = false
)
