package com.cis_ac.cis_ac.data.admin

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.Professional
import kotlinx.coroutines.flow.Flow

interface ProfessionalDirectoryRepository {
    fun watchVerifiedProfessionals(): Flow<Result<List<Professional>>>
}