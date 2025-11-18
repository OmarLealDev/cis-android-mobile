package com.cis_ac.cis_ac.core.model.history

import androidx.compose.runtime.Immutable
import com.cis_ac.cis_ac.core.model.UserRole

@Immutable
data class GeneralInfo(
    val ageYears: Int? = null,
    val weightKg: Float? = null,
    val heightCm: Float? = null,
    val bloodType: String? = null,
    val chronicConditions: List<String> = emptyList(),
    val surgeries: List<String> = emptyList(),
    val habits: Habits = Habits(),
    val emergencyContact: EmergencyContact = EmergencyContact(),
    val updatedAt: Long? = null,
    val updatedBy: UserRef? = null
)

@Immutable
data class Habits(
    val smoker: Boolean = false,
    val alcoholUse: Boolean = false,
    val exerciseFreq: String = ""
)

@Immutable
data class EmergencyContact(
    val name: String = "",
    val phone: String = "",
    val relation: String = ""
)

@Immutable
data class UserRef(
    val uid: String = "",
    val name: String = "",
    val role: UserRole = UserRole.PATIENT
)

@Immutable
data class HistoryEntry(
    val id: String,
    val section: String,
    val date: String,
    val text: String,
    val createdBy: UserRef,
    val createdAt: Long?,
    val updatedBy: UserRef? = null,
    val updatedAt: Long? = null
)
