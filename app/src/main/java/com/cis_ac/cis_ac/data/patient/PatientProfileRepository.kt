package com.cis_ac.cis_ac.data.patient

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.Patient

interface PatientProfileRepository {
    suspend fun getCurrent(): Result<Patient>
    suspend fun update(fullName: String, email: String, phone: String): Result<Unit>
}