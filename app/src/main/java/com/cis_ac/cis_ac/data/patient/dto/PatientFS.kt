package com.cis_ac.cis_ac.data.patient.dto

import com.google.firebase.Timestamp

data class PatientFS(
    val uid: String? = null,
    val email: String? = null,
    val role: String? = null,
    val fullName: String? = null,
    val phone: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val createdAt: Timestamp? = null
)
