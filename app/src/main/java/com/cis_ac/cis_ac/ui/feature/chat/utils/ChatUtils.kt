package com.cis_ac.cis_ac.ui.feature.chat.utils

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.data.userprofile.UserProfileRepository

suspend fun getUserDisplayName(
    userId: String,
    userProfileRepository: UserProfileRepository
): String {
    return when (val userResult = userProfileRepository.getCurrentUserProfile(userId)) {
        is Result.Success -> {
            when (val profile = userResult.data) {
                is com.cis_ac.cis_ac.core.model.Patient -> profile.fullName
                is com.cis_ac.cis_ac.core.model.Professional -> profile.fullName
                is com.cis_ac.cis_ac.core.model.Admin -> profile.fullName
            }
        }
        is Result.Error -> "Usuario"
        is Result.Loading -> "Usuario"
        is Result.UserProfile -> {
            when (val profile = userResult.data) {
                is com.cis_ac.cis_ac.core.model.Patient -> profile.fullName
                is com.cis_ac.cis_ac.core.model.Professional -> profile.fullName
                is com.cis_ac.cis_ac.core.model.Admin -> profile.fullName
            }
        }
    }
}
