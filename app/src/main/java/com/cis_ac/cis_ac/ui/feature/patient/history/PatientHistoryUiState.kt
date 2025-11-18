package com.cis_ac.cis_ac.ui.feature.patient.history

import androidx.compose.runtime.Immutable
import com.cis_ac.cis_ac.core.model.history.GeneralInfo
import com.cis_ac.cis_ac.core.model.history.HistoryEntry

@Immutable
data class PatientHistoryUiState(
    val loading: Boolean = true,
    val error: String = "",

    val generalExpanded: Boolean = false,
    val diagnosesExpanded: Boolean = false,
    val treatmentsExpanded: Boolean = false,
    val medsExpanded: Boolean = false,
    val allergiesExpanded: Boolean = false,

    val general: GeneralInfo = GeneralInfo(),
    val diagnoses: List<HistoryEntry> = emptyList(),
    val treatments: List<HistoryEntry> = emptyList(),
    val medications: List<HistoryEntry> = emptyList(),
    val allergies: List<HistoryEntry> = emptyList(),

    val canSave: Boolean = false,
    val editingNote: HistoryEntry? = null
)

enum class Section { Diagnoses, Treatments, Medications }
