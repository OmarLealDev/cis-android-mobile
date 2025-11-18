package com.cis_ac.cis_ac.ui.feature.home.patient

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime

@Immutable
sealed interface PatientHomeUiState {
    object Loading : PatientHomeUiState
    data class Content(
        val profile: PatientProfile,
        val nextAppointment: NextAppointment?
    ) : PatientHomeUiState
    object Empty : PatientHomeUiState
    data class Error(val message: String) : PatientHomeUiState
}

@Immutable
data class PatientProfile(
    val displayName: String
)

@Immutable
data class NextAppointment(
    val title: String,
    val professionalName: String,
    val dateTime: LocalDateTime
)
