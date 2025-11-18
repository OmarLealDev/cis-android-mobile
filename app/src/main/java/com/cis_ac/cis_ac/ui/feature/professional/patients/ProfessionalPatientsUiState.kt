package com.cis_ac.cis_ac.ui.feature.professional.patients

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime

@Immutable
data class ProPatientItem(
    val uid: String,
    val fullName: String,
    val lastVisit: LocalDateTime?
)

sealed interface ProfessionalPatientsUiState {
    data object Loading : ProfessionalPatientsUiState
    data class Error(val message: String) : ProfessionalPatientsUiState
    data class Content(
        val query: String,
        val order: Order,
        val items: List<ProPatientItem>,
        val visible: List<ProPatientItem>
    ) : ProfessionalPatientsUiState

    enum class Order { LAST_VISIT_DESC, NAME_ASC }
}
