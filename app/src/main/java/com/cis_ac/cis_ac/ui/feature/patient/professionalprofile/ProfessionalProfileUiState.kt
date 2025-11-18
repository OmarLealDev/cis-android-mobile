package com.cis_ac.cis_ac.ui.feature.patient.professionalprofile

import androidx.compose.runtime.Immutable
import com.cis_ac.cis_ac.core.model.Discipline

@Immutable
sealed interface ProfessionalProfileUiState {
    data object Loading : ProfessionalProfileUiState
    data class Error(val message: String) : ProfessionalProfileUiState
    data class Content(
        val uid: String,
        val fullName: String,
        val discipline: Discipline,
        val licenseNumber: String?,
        val expertiz: String?,
        val approach: String?,
        val topics: List<String>,
        val sessionTypes: List<String>,
        val modalities: List<String>,
        val summary: String?,
        val rating: Double?,
        val testimonials: List<Testimonial>
    ) : ProfessionalProfileUiState
}

@Immutable
data class Testimonial(
    val author: String,
    val monthsAgo: Int,
    val text: String,
    val stars: Int
)