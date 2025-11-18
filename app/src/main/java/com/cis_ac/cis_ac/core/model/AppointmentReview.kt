package com.cis_ac.cis_ac.core.model


data class AppointmentReview(
    val appointmentId: String,
    val professionalId: String,
    val patientId: String,
    val status: ReviewStatus,
    val rating: Int? = null,
    val comment: String? = null
)
