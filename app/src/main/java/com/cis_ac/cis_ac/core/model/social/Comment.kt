package com.cis_ac.cis_ac.core.model.social

import androidx.compose.runtime.Immutable
import com.google.firebase.Timestamp

@Immutable
data class Comment(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
