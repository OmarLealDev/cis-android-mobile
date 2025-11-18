package com.cis_ac.cis_ac.ui.feature.home.admin.professionals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.data.admin.FirestoreProfessionalDirectoryRepository
import com.cis_ac.cis_ac.data.admin.ProfessionalDirectoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ProfessionalsViewModel(
    private val repo: ProfessionalDirectoryRepository = FirestoreProfessionalDirectoryRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfessionalsUiState())
    val ui: StateFlow<ProfessionalsUiState> = _ui

    init { observe() }

    private fun observe() {
        repo.watchVerifiedProfessionals()
            .onEach { res ->
                when (res) {
                    is Result.Loading -> _ui.value = _ui.value.copy(loading = true, error = "")
                    is Result.Success -> _ui.value = _ui.value.copy(
                        loading = false, error = "", all = res.data
                    )
                    is Result.Error -> _ui.value = _ui.value.copy(
                        loading = false, error = res.message
                    )

                    is Result.UserProfile<*> -> TODO()
                }
            }
            .catch { e -> _ui.value = _ui.value.copy(loading = false, error = e.message ?: "Error") }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(q: String) { _ui.value = _ui.value.copy(query = q) }
    fun onFilterChange(f: ProFilter) { _ui.value = _ui.value.copy(filter = f) }
}
