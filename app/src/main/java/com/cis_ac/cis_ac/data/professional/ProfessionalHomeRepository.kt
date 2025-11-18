package com.cis_ac.cis_ac.data.professional

import com.cis_ac.cis_ac.ui.feature.home.professional.ProNextAppointmentItem
import com.cis_ac.cis_ac.ui.feature.home.professional.ProfessionalProfile

interface ProfessionalHomeRepository {
    suspend fun loadProfile(): ProfessionalProfile
    suspend fun loadNextAppointments(): List<ProNextAppointmentItem>
}