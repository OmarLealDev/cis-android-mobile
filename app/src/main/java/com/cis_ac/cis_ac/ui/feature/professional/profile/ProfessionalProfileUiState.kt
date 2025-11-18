package com.cis_ac.cis_ac.ui.feature.professional.profile

import androidx.compose.runtime.Immutable
import com.cis_ac.cis_ac.core.model.Discipline
import com.cis_ac.cis_ac.core.model.Gender
import com.cis_ac.cis_ac.core.model.Modality
import com.cis_ac.cis_ac.core.model.Population
import com.cis_ac.cis_ac.core.model.Sessions

sealed interface ProfessionalProfileUiState {
    data object Loading : ProfessionalProfileUiState
    data class Error(val message: String) : ProfessionalProfileUiState

    data class Content(
        val editing: Boolean,
        val saving: Boolean,
        val verified: Boolean,

        val fullName: String,
        val email: String,
        val phone: String,
        val dob: String,
        val gender: Gender,

        val discipline: Discipline?,
        val licenseNumber: String,
        val speciality: String,
        val approach: String,
        val topics: String,
        val expertiz: String,

        val modalities: Set<Modality>,
        val sessionTypes: Set<Sessions>,
        val populations: Set<Population>,

        val semblance: String,

        val cvUrl: String?,
        val licenseUrl: String?
    ) : ProfessionalProfileUiState
}
