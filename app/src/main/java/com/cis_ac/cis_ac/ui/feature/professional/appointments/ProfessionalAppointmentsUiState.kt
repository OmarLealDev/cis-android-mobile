package com.cis_ac.cis_ac.ui.feature.professional.appointments

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime

@Immutable
data class ProAppointmentItem(
    val id: String,
    val patientName: String,
    val dateTime: LocalDateTime,
    val confirmed: Boolean,
    val notes: String
)

sealed interface ProfessionalAppointmentsUiState {
    data object Loading : ProfessionalAppointmentsUiState
    data class Error(val message: String) : ProfessionalAppointmentsUiState
    data class Content(
        val upcoming: List<ProAppointmentItem>,
        val past: List<ProAppointmentItem>
    ) : ProfessionalAppointmentsUiState
}
