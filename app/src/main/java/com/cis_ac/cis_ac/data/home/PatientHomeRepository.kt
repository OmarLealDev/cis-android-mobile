package com.cis_ac.cis_ac.data.home
import com.cis_ac.cis_ac.ui.feature.home.patient.NextAppointment
import com.cis_ac.cis_ac.ui.feature.home.patient.PatientProfile

interface PatientHomeRepository {
    suspend fun loadProfile(): PatientProfile
    suspend fun loadNextAppointmentOrNull(): NextAppointment?
}