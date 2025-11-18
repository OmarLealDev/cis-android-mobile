package com.cis_ac.cis_ac.ui.feature.patient.networks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.social.Post
import com.cis_ac.cis_ac.data.social.FirestoreSocialRepository
import com.cis_ac.cis_ac.data.social.SocialRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PatientFeedUiState(
    val loading: Boolean = true,
    val error: String = "",
    val posts: List<Post> = emptyList()
)

class PatientNetworksViewModel(
    private val repo: SocialRepository = FirestoreSocialRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(PatientFeedUiState())
    val ui: StateFlow<PatientFeedUiState> = _ui

    private var streamJob: Job? = null

    init {
        refreshOnce()
    }

    fun refreshOnce() = viewModelScope.launch {
        _ui.value = _ui.value.copy(loading = true, error = "")
        val result = when (repo) {
            is FirestoreSocialRepository -> repo.loadFeedPageWithLikes(limit = 50L, startAfterPostId = null)
            else -> repo.loadFeedPage(limit = 50L, startAfterPostId = null)
        }
        when (result) {
            is Result.Success -> _ui.value = _ui.value.copy(loading = false, posts = result.data, error = "")
            is Result.Error   -> _ui.value = _ui.value.copy(loading = false, error = result.message)
            else -> Unit
        }
    }

    fun toggleLike(postId: String) = viewModelScope.launch {
        when (repo.toggleLike(postId)) {
            is Result.Success -> {
                val current = _ui.value.posts.toMutableList()
                val idx = current.indexOfFirst { it.id == postId }
                if (idx >= 0) {
                    val p = current[idx]
                    val nowLiked = !p.likedByMe
                    val newCount = (p.likeCount + if (nowLiked) 1 else -1).coerceAtLeast(0)
                    current[idx] = p.copy(likedByMe = nowLiked, likeCount = newCount)
                    _ui.value = _ui.value.copy(posts = current)
                }
            }
            is Result.Error -> {
            }
            else -> Unit
        }
    }
}
