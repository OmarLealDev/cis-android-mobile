package com.cis_ac.cis_ac.ui.feature.patient.appointments.new

import androidx.compose.runtime.Immutable
import com.cis_ac.cis_ac.core.model.Discipline

@Immutable
data class NewAppointmentUiState(
    // combos
    val disciplines: List<Discipline> = Discipline.entries,
    val selectedDiscipline: Discipline? = null,
    val professionals: List<ProfessionalOption> = emptyList(),
    val selectedProfessional: ProfessionalOption? = null,

    val enabledDays: Set<Int> = emptySet(),
    val availableHours: List<Int> = emptyList(),
    val disabledHours: Set<Int> = emptySet(),

    val selectedEpochDay: Long? = null,
    val selectedHour: Int? = null,

    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String = "",
    val success: Boolean = false
)

@Immutable
data class ProfessionalOption(
    val uid: String,
    val name: String
)
