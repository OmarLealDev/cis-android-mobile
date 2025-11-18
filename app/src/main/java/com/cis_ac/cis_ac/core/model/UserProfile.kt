package com.cis_ac.cis_ac.core.model

sealed interface UserProfile {
    val uid: String
    val email: String
    val role: UserRole
}



