package com.cis_ac.cis_ac.ui.feature.patient.profile

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PatientProfileUiState {
    data object Loading : PatientProfileUiState
    data class Error(val message: String) : PatientProfileUiState
    data class Content(
        val uid: String,
        val fullName: String,
        val email: String,
        val phone: String,
        val isEditing: Boolean = false,
        val isSaving: Boolean = false,
        val saveOk: Boolean = false
    ) : PatientProfileUiState
}
