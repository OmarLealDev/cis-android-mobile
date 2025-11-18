package com.cis_ac.cis_ac.data.patient

import com.cis_ac.cis_ac.core.model.Gender
import com.cis_ac.cis_ac.core.model.Patient
import com.cis_ac.cis_ac.core.model.UserRole
import com.cis_ac.cis_ac.data.patient.dto.PatientFS

internal fun PatientFS.toDomain(): Patient? {
    val id = uid ?: return null
    val mail = email ?: return null

    val roleEnum = role?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
        ?: UserRole.PATIENT

    val genderEnum = gender?.let { runCatching { Gender.valueOf(it) }.getOrNull() }
        ?: Gender.Unspecified

    return Patient(
        uid = id,
        email = mail,
        role = roleEnum,
        fullName = fullName.orEmpty(),
        phone = phone.orEmpty(),
        dob = dob.orEmpty(),
        gender = genderEnum
    )
}
