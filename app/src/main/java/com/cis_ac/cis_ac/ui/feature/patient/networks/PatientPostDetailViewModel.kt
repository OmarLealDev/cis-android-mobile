package com.cis_ac.cis_ac.ui.feature.patient.networks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.social.Comment
import com.cis_ac.cis_ac.core.model.social.Post
import com.cis_ac.cis_ac.data.social.FirestoreSocialRepository
import com.cis_ac.cis_ac.data.social.SocialRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PatientPostDetailUiState(
    val loading: Boolean = true,
    val error: String = "",
    val post: Post? = null,
    val comments: List<Comment> = emptyList()
)

class PatientPostDetailViewModel(
    private val postId: String,
    private val repo: SocialRepository = FirestoreSocialRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(PatientPostDetailUiState())
    val ui: StateFlow<PatientPostDetailUiState> = _ui

    private var commentsJob: Job? = null

    fun load() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = "")
            when (val res = repo.getPost(postId)) {
                is Result.Success -> {
                    _ui.value = _ui.value.copy(loading = false, post = res.data, error = "")
                    listenComments()
                }
                is Result.Error -> _ui.value = _ui.value.copy(loading = false, error = res.message)
                else -> Unit
            }
        }
    }

    private fun listenComments() {
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            repo.listenComments(postId, limit = 200L).collect { list ->
                _ui.value = _ui.value.copy(comments = list)
            }
        }
    }

    fun addComment(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            when (val res = repo.addComment(postId, trimmed)) {
                is Result.Success -> {
                    _ui.value.post?.let { p ->
                        _ui.value = _ui.value.copy(post = p.copy(commentCount = p.commentCount + 1))
                    }
                }
                is Result.Error -> {
                }
                else -> Unit
            }
        }
    }

    fun toggleLike() {
        val cur = _ui.value.post ?: return
        val liked = !cur.likedByMe
        val newCount = (cur.likeCount + if (liked) 1 else -1).coerceAtLeast(0)
        _ui.value = _ui.value.copy(post = cur.copy(likedByMe = liked, likeCount = newCount))
        viewModelScope.launch { repo.toggleLike(postId) }
    }
}

class PatientPostDetailVMFactory(
    private val postId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PatientPostDetailViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return PatientPostDetailViewModel(postId) as T
    }
}
