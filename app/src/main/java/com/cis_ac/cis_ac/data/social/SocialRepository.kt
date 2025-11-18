package com.cis_ac.cis_ac.data.social

import android.net.Uri
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.social.Comment
import com.cis_ac.cis_ac.core.model.social.Post
import kotlinx.coroutines.flow.Flow

interface SocialRepository {

    suspend fun createPost(text: String, imageUri: Uri?): Result<Post>
    suspend fun deletePost(postId: String): Result<Unit>
    suspend fun getPost(postId: String): Result<Post>

    suspend fun loadFeedPage(limit: Long, startAfterPostId: String? = null): Result<List<Post>>
    fun listenLatest(limit: Long): Flow<List<Post>>

    suspend fun toggleLike(postId: String): Result<Boolean>

    suspend fun addComment(postId: String, text: String): Result<Unit>
    fun listenComments(postId: String, limit: Long): Flow<List<Comment>>
}
