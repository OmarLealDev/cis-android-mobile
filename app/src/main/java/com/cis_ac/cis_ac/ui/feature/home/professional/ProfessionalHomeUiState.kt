package com.cis_ac.cis_ac.ui.feature.home.professional

import androidx.compose.runtime.Immutable

@Immutable
data class ProfessionalProfile(
    val displayName: String = "Profesional"
)

@Immutable
data class ProNextAppointmentItem(
    val id: String,
    val patientId: String,
    val patientName: String,
    val dateTimeMillis: Long,
    val confirmed: Boolean,
    val disciplineLabel: String,
    val firstTime: Boolean = false,
    val title: String = "Consulta con $patientName"
)
sealed interface ProfessionalHomeUiState {
    data object Loading : ProfessionalHomeUiState
    data class Content(
        val profile: ProfessionalProfile,
        val nextAppointments: List<ProNextAppointmentItem>
    ) : ProfessionalHomeUiState
    data class Error(val message: String) : ProfessionalHomeUiState
}
