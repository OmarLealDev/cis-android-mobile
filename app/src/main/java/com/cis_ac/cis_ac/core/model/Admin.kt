package com.cis_ac.cis_ac.core.model

data class Admin(
    override val uid: String = "",
    override val email: String = "",
    override val role: UserRole = UserRole.ADMIN,
    val fullName: String = ""
) : UserProfile