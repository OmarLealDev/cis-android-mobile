package com.cis_ac.cis_ac.data.admin

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.Professional
import kotlinx.coroutines.flow.Flow

interface AdminRequestsRepository {
    /** Profesionales con verified=false (pendientes). Tiempo real. */
    fun watchPendingProfessionals(): Flow<Result<List<Professional>>>

    /** Obtiene un profesional por uid (para detalle). */
    suspend fun getProfessional(uid: String): Result<Professional>

    /** Aprueba (verified=true, active=true). */
    suspend fun approve(uid: String): Result<Unit>

    /** Rechaza (verified=false, active=false). */
    suspend fun reject(uid: String): Result<Unit>
}
