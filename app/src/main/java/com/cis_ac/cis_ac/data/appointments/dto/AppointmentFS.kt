package com.cis_ac.cis_ac.data.appointments.dto

data class AppointmentFS(
    val id: String? = null,
    val patientId: String? = null,
    val professionalId: String? = null,
    val discipline: String? = null,
    val dateEpochDay: Long? = null,
    val hour24: Long? = null,
    val notes: String? = null,
    val active: Boolean? = null,
    val confirmed: Boolean? = null,
    val createdAt: Long? = null
)
