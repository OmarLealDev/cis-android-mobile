package com.cis_ac.cis_ac.ui.feature.home.professional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.data.professional.ProfessionalHomeRepository
import com.cis_ac.cis_ac.data.professional.FirestoreProfessionalHomeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfessionalHomeViewModel(
    private val repo: ProfessionalHomeRepository = FirestoreProfessionalHomeRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow<ProfessionalHomeUiState>(ProfessionalHomeUiState.Loading)
    val ui: StateFlow<ProfessionalHomeUiState> = _ui

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _ui.value = ProfessionalHomeUiState.Loading
            try {
                val profileDef = async { repo.loadProfile() }
                val apptsDef   = async { repo.loadNextAppointments() }

                val profile = profileDef.await()
                val upcoming = apptsDef.await()
                    .sortedBy { it.dateTimeMillis }
                    .take(2)

                _ui.value = ProfessionalHomeUiState.Content(
                    profile = profile,
                    nextAppointments = upcoming
                )
            } catch (t: Throwable) {
                _ui.value = ProfessionalHomeUiState.Error(t.message ?: "Error al cargar home")
            }
        }
    }
}
