package com.cis_ac.cis_ac.ui.feature.professional.networks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.social.Comment
import com.cis_ac.cis_ac.core.model.social.Post
import com.cis_ac.cis_ac.data.social.FirestoreSocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfessionalPostDetailViewModel(
    private val postId: String,
    private val repo: FirestoreSocialRepository = FirestoreSocialRepository()
) : ViewModel() {

    data class Ui(
        val loading: Boolean = true,
        val error: String = "",
        val post: Post? = null,
        val comments: List<Comment> = emptyList()
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui

    fun load() = viewModelScope.launch {
        _ui.value = _ui.value.copy(loading = true, error = "")
        when (val res = repo.getPost(postId)) {
            is Result.Success -> _ui.value = _ui.value.copy(loading = false, post = res.data)
            is Result.Error   -> _ui.value = _ui.value.copy(loading = false, error = res.message)
            else -> Unit
        }
        viewModelScope.launch {
            repo.listenComments(postId, limit = 200).collect { list ->
                _ui.value = _ui.value.copy(comments = list)
            }
        }
    }

    fun toggleLike() = viewModelScope.launch {
        val p = _ui.value.post ?: return@launch
        val nowLiked = !p.likedByMe
        _ui.value = _ui.value.copy(post = p.copy(likedByMe = nowLiked, likeCount = p.likeCount + if (nowLiked) 1 else -1))
        when (repo.toggleLike(postId)) {
            is Result.Success -> Unit
            is Result.Error -> {
                val revert = _ui.value.post ?: return@launch
                _ui.value = _ui.value.copy(post = revert.copy(likedByMe = !nowLiked, likeCount = revert.likeCount + if (nowLiked) -1 else 1))
            }
            else -> Unit
        }
    }

    fun addComment(text: String) = viewModelScope.launch {
        when (val res = repo.addComment(postId, text)) {
            is Result.Success -> Unit
            is Result.Error   -> _ui.value = _ui.value.copy(error = res.message)
            else -> Unit
        }
    }
}

