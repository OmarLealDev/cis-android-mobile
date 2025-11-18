package com.cis_ac.cis_ac.data.patient.professionals

import com.cis_ac.cis_ac.ui.feature.patient.professionals.PatientProfessionalItem
import com.cis_ac.cis_ac.ui.feature.patient.professionals.ProfessionalsFilters

interface PatientProfessionalsRepository {
    suspend fun fetchProfessionals(filters: ProfessionalsFilters): List<PatientProfessionalItem>
}