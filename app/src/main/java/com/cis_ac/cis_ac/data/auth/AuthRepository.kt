package com.cis_ac.cis_ac.data.auth

import com.cis_ac.cis_ac.core.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun authState(): Flow<Boolean>
    suspend fun signIn(email: String, password: String): Result<String>
    suspend fun signUp(email: String, password: String): Result<String>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUserId(): String?
}