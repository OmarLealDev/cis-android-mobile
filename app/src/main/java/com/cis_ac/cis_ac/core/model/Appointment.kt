package com.cis_ac.cis_ac.core.model

data class Appointment(
    val id: String = "",
    val patientId: String?,
    val professionalId: String,
    val discipline: Discipline,
    val dateEpochDay: Long,
    val hour24: Int,
    val notes: String = "",
    val active: Boolean = true,
    val confirmed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)