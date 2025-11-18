package com.cis_ac.cis_ac.ui.feature.patient.professionals

import androidx.compose.runtime.Immutable
import com.cis_ac.cis_ac.core.model.Discipline

@Immutable
data class PatientProfessionalItem(
    val uid: String,
    val fullName: String,
    val discipline: Discipline,
    val rating: Double? = null
)

@Immutable
data class ProfessionalsFilters(
    val query: String = "",
    val discipline: Discipline? = null,
    val populationType: String? = null,
    val modality: String? = null
)

sealed interface PatientProfessionalsUiState {
    object Loading : PatientProfessionalsUiState
    data class Content(
        val items: List<PatientProfessionalItem>,
        val filters: ProfessionalsFilters
    ) : PatientProfessionalsUiState
    data class Error(val message: String) : PatientProfessionalsUiState
}

enum class PatientProfessionalsNav { OpenProfile, Schedule }
