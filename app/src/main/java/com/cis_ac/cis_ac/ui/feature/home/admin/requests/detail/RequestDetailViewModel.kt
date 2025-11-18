package com.cis_ac.cis_ac.ui.feature.home.admin.requests.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.data.admin.AdminRequestsRepository
import com.cis_ac.cis_ac.data.admin.FirestoreAdminRequestsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RequestDetailViewModel(
    private val repo: AdminRequestsRepository = FirestoreAdminRequestsRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow<RequestDetailUiState>(RequestDetailUiState.Loading)
    val ui: StateFlow<RequestDetailUiState> = _ui

    fun load(uid: String) {
        viewModelScope.launch {
            _ui.value = RequestDetailUiState.Loading
            when (val res = repo.getProfessional(uid)) {
                is Result.Success -> _ui.value = RequestDetailUiState.Content(res.data)
                is Result.Error -> _ui.value = RequestDetailUiState.Error(res.message)
                else -> Unit
            }
        }
    }

    fun approve(uid: String) {
        viewModelScope.launch {
            when (val res = repo.approve(uid)) {
                is Result.Success -> _ui.value = RequestDetailUiState.Done
                is Result.Error -> _ui.value = RequestDetailUiState.Error(res.message)
                else -> Unit
            }
        }
    }

    fun reject(uid: String) {
        viewModelScope.launch {
            when (val res = repo.reject(uid)) {
                is Result.Success -> _ui.value = RequestDetailUiState.Done
                is Result.Error -> _ui.value = RequestDetailUiState.Error(res.message)
                else -> Unit
            }
        }
    }
}
