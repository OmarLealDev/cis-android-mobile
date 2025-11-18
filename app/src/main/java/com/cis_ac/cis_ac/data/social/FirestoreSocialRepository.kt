package com.cis_ac.cis_ac.data.social

import android.net.Uri
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.social.Comment
import com.cis_ac.cis_ac.core.model.social.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.UUID

class FirestoreSocialRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : SocialRepository {

    private val colPosts get() = db.collection("posts")

    suspend fun loadFeedPageWithLikes(
        limit: Long,
        startAfterPostId: String?
    ): Result<List<Post>> = try {
        var q: Query = colPosts.orderBy("createdAt", Query.Direction.DESCENDING).limit(limit)
        if (startAfterPostId != null) {
            val anchor = colPosts.document(startAfterPostId).get().await()
            if (anchor.exists()) q = q.startAfter(anchor)
        }

        val me = auth.currentUser?.uid
        val snaps = q.get().await()
        val base = snaps.documents.map { it.toPost() }

        if (me == null) {
            Result.Success(base)
        } else {
            val enriched = coroutineScope {
                base.map { post ->
                    async {
                        val like = colPosts.document(post.id)
                            .collection("likes").document(me).get().await()
                        post.copy(likedByMe = like.exists())
                    }
                }.awaitAll()
            }
            Result.Success(enriched)
        }
    } catch (e: Exception) {
        Result.Error(e.message ?: "Error al cargar feed")
    }


    override suspend fun getPost(postId: String): Result<Post> {
        return try {
            val me = auth.currentUser?.uid
            val snap = colPosts.document(postId).get().await()
            if (!snap.exists()) return Result.Error("Post no encontrado")
            val base = snap.toPost()
            val liked = if (me != null) isLikedByUser(postId, me) else false
            Result.Success(base.copy(likedByMe = liked))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar post")
        }
    }

    override suspend fun createPost(text: String, imageUri: Uri?): Result<Post> {
        val uid = auth.currentUser?.uid ?: return Result.Error("No auth")
        val authorName = fetchUserName(uid)

        return try {
            val postRef = colPosts.document()

            var imageUrl: String? = null
            if (imageUri != null) {
                val storageRef = storage.reference
                    .child("posts/${postRef.id}/${UUID.randomUUID()}.jpg")
                storageRef.putFile(imageUri).await()
                imageUrl = storageRef.downloadUrl.await().toString()
            }

            val data = hashMapOf(
                "authorId" to uid,
                "authorName" to authorName,
                "authorRole" to "PROFESSIONAL",
                "text" to text,
                "imageUrl" to imageUrl,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "likeCount" to 0L,
                "commentCount" to 0L
            )
            postRef.set(data).await()

            val snap = postRef.get().await()
            Result.Success(snap.toPost())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al crear post")
        }
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.Error("No auth")
            val ref = colPosts.document(postId)
            val snap = ref.get().await()
            val authorId = snap.getString("authorId")
            if (authorId != uid) return Result.Error("No autorizado")
            ref.delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al eliminar post")
        }
    }

    override suspend fun loadFeedPage(limit: Long, startAfterPostId: String?): Result<List<Post>> = try {
        var q: Query = colPosts.orderBy("createdAt", Query.Direction.DESCENDING).limit(limit)
        if (startAfterPostId != null) {
            val anchor = colPosts.document(startAfterPostId).get().await()
            if (anchor.exists()) q = q.startAfter(anchor)
        }
        val snaps = q.get().await()
        Result.Success(snaps.documents.map { it.toPost() })
    } catch (e: Exception) {
        Result.Error(e.message ?: "Error al cargar feed")
    }

    override fun listenLatest(limit: Long): Flow<List<Post>> = callbackFlow {
        val reg = colPosts
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { qs, _ ->
                val posts = qs?.documents?.map { it.toPost() }.orEmpty()
                trySend(posts)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun toggleLike(postId: String): Result<Boolean> {
        val uid = auth.currentUser?.uid ?: return Result.Error("No auth")
        val likeRef = colPosts.document(postId).collection("likes").document(uid)
        val postRef = colPosts.document(postId)

        return try {
            db.runTransaction { tx ->
                val likeSnap = tx.get(likeRef)
                val hasLike = likeSnap.exists()
                if (hasLike) {
                    tx.delete(likeRef)
                    tx.update(postRef, "likeCount", FieldValue.increment(-1L))
                } else {
                    tx.set(likeRef, mapOf("createdAt" to FieldValue.serverTimestamp()))
                    tx.update(postRef, "likeCount", FieldValue.increment(1L))
                }
            }.await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "toggleLike error")
        }
    }


    override suspend fun addComment(postId: String, text: String): Result<Unit> = try {
        val uid = auth.currentUser?.uid ?: return Result.Error("Sin sesión")
        val name = fetchUserName(uid)

        val commentRef = colPosts.document(postId)
            .collection("comments").document()

        val data = mapOf(
            "id" to commentRef.id,
            "authorId" to uid,
            "authorName" to name,
            "text" to text,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.runBatch { b ->
            b.set(commentRef, data)
            b.update(colPosts.document(postId), "commentCount", FieldValue.increment(1L))
        }.await()

        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "No se pudo comentar")
    }

    override fun listenComments(postId: String, limit: Long): Flow<List<Comment>> = callbackFlow {
        val reg = colPosts
            .document(postId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limit(limit)
            .addSnapshotListener { qs, e ->
                if (e != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = qs?.documents.orEmpty().map { it.toComment() }
                trySend(list)
            }
        awaitClose { reg.remove() }
    }


    private suspend fun fetchUserName(uid: String): String {
        try {
            val pro = db.collection("professionals").document(uid).get().await()
            pro.getString("fullName")?.let { if (it.isNotBlank()) return it }
        } catch (_: Exception) {}

        try {
            val pat = db.collection("patients").document(uid).get().await()
            pat.getString("fullName")?.let { if (it.isNotBlank()) return it }
        } catch (_: Exception) {}

        auth.currentUser?.displayName?.let { if (it.isNotBlank()) return it }

        val email = auth.currentUser?.email
        if (!email.isNullOrBlank()) return email.substringBefore('@')

        return "Usuario"
    }

    private suspend fun isLikedByUser(postId: String, uid: String): Boolean {
        val like = colPosts.document(postId).collection("likes").document(uid).get().await()
        return like.exists()
    }

    private fun DocumentSnapshot.toPost(): Post = Post(
        id = id,
        authorId = getString("authorId") ?: "",
        authorName = getString("authorName") ?: "",
        authorAvatarUrl = getString("authorAvatarUrl"),
        authorRole = getString("authorRole") ?: "PROFESSIONAL",
        text = getString("text") ?: "",
        imageUrl = getString("imageUrl"),
        createdAt = getTimestamp("createdAt"),
        updatedAt = getTimestamp("updatedAt"),
        likeCount = getLong("likeCount") ?: 0L,
        commentCount = getLong("commentCount") ?: 0L,
        likedByMe = false
    )

    private fun DocumentSnapshot.toComment(): Comment = Comment(
        id = id,
        authorId = getString("authorId") ?: "",
        authorName = getString("authorName") ?: "",
        text = getString("text") ?: "",
        createdAt = getTimestamp("createdAt"),
        updatedAt = getTimestamp("updatedAt")
    )
}
