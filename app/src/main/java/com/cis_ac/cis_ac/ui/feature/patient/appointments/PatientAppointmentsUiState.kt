package com.cis_ac.cis_ac.ui.feature.patient.appointments

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime

@Immutable
sealed interface PatientAppointmentsUiState {
    data object Loading : PatientAppointmentsUiState
    data class Error(val message: String) : PatientAppointmentsUiState
    data class Content(
        val upcoming: List<AppointmentItem>,
        val past: List<AppointmentItem>
    ) : PatientAppointmentsUiState
}

data class AppointmentItem(
    val id: String,
    val professionalName: String,
    val disciplineLabel: String,
    val dateTime: java.time.LocalDateTime,
    val durationMinutes: Int,
    val confirmed: Boolean = false
)