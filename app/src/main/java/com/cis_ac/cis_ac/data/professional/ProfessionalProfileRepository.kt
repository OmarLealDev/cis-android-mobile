package com.cis_ac.cis_ac.data.professional

import com.cis_ac.cis_ac.core.Result              // ← ESTE import
import com.cis_ac.cis_ac.core.model.Professional

interface ProfessionalProfileRepository {
    suspend fun getProfessional(uid: String): Result<Professional>
    suspend fun updateProfessional(uid: String, patch: ProfessionalUpdate): Result<Unit>
}
data class ProfessionalUpdate(
    val fullName: String? = null,
    val phone: String? = null,
    val dob: String? = null,
    val gender: com.cis_ac.cis_ac.core.model.Gender? = null,
    val licenseNumber: String? = null,
    val speciality: String? = null,
    val approach: String? = null,
    val topics: String? = null,
    val expertiz: String? = null,
    val modalities: Set<com.cis_ac.cis_ac.core.model.Modality>? = null,
    val sessionTypes: Set<com.cis_ac.cis_ac.core.model.Sessions>? = null,
    val populations: Set<com.cis_ac.cis_ac.core.model.Population>? = null,
    val semblance: String? = null,
)
