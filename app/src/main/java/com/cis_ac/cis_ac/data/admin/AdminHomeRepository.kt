package com.cis_ac.cis_ac.data.admin

interface AdminHomeRepository {
    suspend fun getActiveProfessionalsCount(): Int
    suspend fun getPendingRequestsCount(): Int
    suspend fun getAdminDisplayName(): String
}
