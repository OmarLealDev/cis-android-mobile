package com.cis_ac.cis_ac.ui.feature.home.admin.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.data.admin.AdminRequestsRepository
import com.cis_ac.cis_ac.data.admin.FirestoreAdminRequestsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class RequestsViewModel(
    private val repo: AdminRequestsRepository = FirestoreAdminRequestsRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(RequestsUiState())
    val ui: StateFlow<RequestsUiState> = _ui

    init { observe() }

    private fun observe() {
        repo.watchPendingProfessionals()
            .onEach { res ->
                when (res) {
                    is Result.Loading -> _ui.value = _ui.value.copy(loading = true, error = "")
                    is Result.Success -> _ui.value = _ui.value.copy(loading = false, pending = res.data, error = "")
                    is Result.Error -> _ui.value = _ui.value.copy(loading = false, error = res.message)
                    is Result.UserProfile<*> -> TODO()
                }
            }
            .catch { e -> _ui.value = _ui.value.copy(loading = false, error = e.message ?: "Error") }
            .launchIn(viewModelScope)
    }
}
