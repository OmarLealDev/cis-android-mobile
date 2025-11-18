package com.cis_ac.cis_ac.core.model

data class PendingReview(
    val appointmentId: String,
    val professionalId: String,
    val professionalName: String,
    val dateTimeMillis: Long
)