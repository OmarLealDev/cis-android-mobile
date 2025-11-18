package com.cis_ac.cis_ac.data.userprofile

import com.cis_ac.cis_ac.core.model.UserProfile
import com.cis_ac.cis_ac.core.model.UserRole
import com.cis_ac.cis_ac.core.Result

interface UserProfileRepository {
    suspend fun createUserProfile(userProfile: UserProfile): Result<String>
    suspend fun getUserProfile(uid: String, role: UserRole): Result<UserProfile>

    suspend fun getCurrentUserProfile(uid: String): Result<UserProfile>
}