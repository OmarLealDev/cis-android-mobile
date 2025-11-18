package com.cis_ac.cis_ac.ui.feature.professional.networks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.social.Post
import com.cis_ac.cis_ac.data.social.FirestoreSocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfessionalNetworksViewModel(
    private val repo: FirestoreSocialRepository = FirestoreSocialRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfessionalNetworksUiState(loading = true))
    val ui: StateFlow<ProfessionalNetworksUiState> = _ui

    fun refreshOnce() = viewModelScope.launch {
        _ui.value = _ui.value.copy(loading = true, error = "")
        when (val res = repo.loadFeedPageWithLikes(limit = 20, startAfterPostId = null)) {
            is Result.Success -> _ui.value = _ui.value.copy(loading = false, posts = res.data)
            is Result.Error   -> _ui.value = _ui.value.copy(loading = false, error = res.message)
            else -> Unit
        }
    }

    fun toggleLike(postId: String) = viewModelScope.launch {
        val before = _ui.value.posts
        val optimistic = before.map { p ->
            if (p.id == postId) {
                val nowLiked = !p.likedByMe
                p.copy(
                    likedByMe = nowLiked,
                    likeCount = p.likeCount + if (nowLiked) 1 else -1
                )
            } else p
        }
        _ui.value = _ui.value.copy(posts = optimistic)

        when (val res = repo.toggleLike(postId)) {
            is Result.Success -> Unit
            is Result.Error   -> {
                val reverted = before
                _ui.value = _ui.value.copy(posts = reverted, error = res.message)
            }
            else -> Unit
        }
    }
}
