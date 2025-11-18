package com.cis_ac.cis_ac.ui.feature.home.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.data.home.FirestorePatientHomeRepository
import com.cis_ac.cis_ac.data.home.PatientHomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PatientHomeViewModel(
    private val repository: PatientHomeRepository = FirestorePatientHomeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PatientHomeUiState>(PatientHomeUiState.Loading)
    val uiState: StateFlow<PatientHomeUiState> = _uiState

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = PatientHomeUiState.Loading
            try {
                val profile = repository.loadProfile()
                val next = repository.loadNextAppointmentOrNull()
                _uiState.value = PatientHomeUiState.Content(profile, next)
            } catch (t: Throwable) {
                _uiState.value = PatientHomeUiState.Error(t.message ?: "Error desconocido")
            }
        }
    }
}
