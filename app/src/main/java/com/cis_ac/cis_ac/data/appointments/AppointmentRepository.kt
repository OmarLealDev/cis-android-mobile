package com.cis_ac.cis_ac.data.appointments

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.Appointment
import com.cis_ac.cis_ac.core.model.Discipline

interface AppointmentRepository {
    suspend fun listProfessionalsByDiscipline(discipline: Discipline): Result<List<MinimalProfessional>>
    suspend fun getProfessionalSchedule(proId: String): Result<Map<Int, List<Int>>>
    suspend fun getBookedHours(proId: String, dateEpochDay: Long): Result<Set<Int>>
    suspend fun create(appointment: Appointment): Result<String>
    suspend fun checkAvailability(
        proId: String,
        dateEpochDay: Long,
        hour24: Int
    ): Result<Boolean>

    suspend fun confirm(appointmentId: String, confirmed: Boolean): Result<Unit>
    suspend fun cancel(appointmentId: String, reason: String? = null): Result<Unit>

    suspend fun listPatientAppointments(patientId: String): Result<List<Appointment>>
    suspend fun listProfessionalAppointments(proId: String, dateEpochDay: Long? = null): Result<List<Appointment>>
    suspend fun getById(appointmentId: String): Result<com.cis_ac.cis_ac.core.model.Appointment>

}

data class MinimalProfessional(
    val uid: String,
    val fullName: String,
    val mainDiscipline: Discipline
)
