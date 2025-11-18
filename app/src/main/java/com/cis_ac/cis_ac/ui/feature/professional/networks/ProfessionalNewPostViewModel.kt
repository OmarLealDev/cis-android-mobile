package com.cis_ac.cis_ac.ui.feature.professional.networks

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.data.social.FirestoreSocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfessionalNewPostViewModel(
    private val repo: FirestoreSocialRepository = FirestoreSocialRepository()
) : ViewModel() {

    data class Ui(
        val text: String = "",
        val imageUri: Uri? = null,
        val loading: Boolean = false,
        val error: String = "",
        val success: Boolean = false
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui

    fun onTextChange(v: String) { _ui.value = _ui.value.copy(text = v) }
    fun onImagePicked(uri: Uri?) { _ui.value = _ui.value.copy(imageUri = uri) }

    fun submit() = viewModelScope.launch {
        val s = _ui.value
        if (s.text.isBlank() && s.imageUri == null) {
            _ui.value = s.copy(error = "Agrega texto o imagen")
            return@launch
        }
        _ui.value = s.copy(loading = true, error = "")
        when (val res = repo.createPost(s.text.trim(), s.imageUri)) {
            is Result.Success -> _ui.value = s.copy(loading = false, success = true)
            is Result.Error   -> _ui.value = s.copy(loading = false, error = res.message)
            else -> Unit
        }
    }

    fun consumeSuccess() { _ui.value = _ui.value.copy(success = false) }
}
