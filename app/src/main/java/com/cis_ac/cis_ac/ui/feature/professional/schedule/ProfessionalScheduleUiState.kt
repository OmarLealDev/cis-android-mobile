package com.cis_ac.cis_ac.ui.feature.professional.schedule

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ProfessionalScheduleUiState {
    data object Loading : ProfessionalScheduleUiState
    data class Error(val message: String) : ProfessionalScheduleUiState
    data class Content(
        val days: List<DayHours>,
        val saving: Boolean,
        val canSave: Boolean,
        val canDiscard: Boolean,
        val invalidDays: Set<Int>
    ) : ProfessionalScheduleUiState
}

@Immutable
data class DayHours(
    val day: Int,
    val hours: List<Int>
)
