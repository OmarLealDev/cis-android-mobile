package com.cis_ac.cis_ac.ui.feature.patient.professionals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.data.patient.professionals.PatientProfessionalsRepository
import com.cis_ac.cis_ac.data.patient.professionals.FirestorePatientProfessionalsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PatientProfessionalsViewModel(
    private val repo: PatientProfessionalsRepository = FirestorePatientProfessionalsRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow<PatientProfessionalsUiState>(
        PatientProfessionalsUiState.Loading
    )
    val ui: StateFlow<PatientProfessionalsUiState> = _ui

    private var currentFilters = ProfessionalsFilters()

    init { reload() }

    fun onQueryChange(q: String) {
        currentFilters = currentFilters.copy(query = q)
        reload()
    }

    fun onDisciplineChange(d: com.cis_ac.cis_ac.core.model.Discipline?) {
        currentFilters = currentFilters.copy(discipline = d)
        reload()
    }

    fun onPopulationChange(value: String?) {
        currentFilters = currentFilters.copy(populationType = value)
        reload()
    }

    fun onModalityChange(value: String?) {
        currentFilters = currentFilters.copy(modality = value)
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            _ui.value = PatientProfessionalsUiState.Loading
            try {
                val list = repo.fetchProfessionals(currentFilters)
                _ui.value = PatientProfessionalsUiState.Content(list, currentFilters)
            } catch (t: Throwable) {
                _ui.value = PatientProfessionalsUiState.Error(t.message ?: "Error al cargar profesionales")
            }
        }
    }
    fun resetFilters() {
        _ui.update { current ->
            when (current) {
                is PatientProfessionalsUiState.Content -> {
                    val cleared = current.filters.copy(
                        query = "",
                        discipline = null,
                        populationType = null,
                        modality = null
                    )
                    current.copy(filters = cleared)
                }
                else -> current
            }
        }
        reload()
    }
}
