package com.cis_ac.cis_ac.core.model

data class Professional(
    override val uid: String = "",
    override val email: String,
    override val role: UserRole,
    val fullName: String = "",
    val phone: String = "",
    val dob: String = "",
    val gender: Gender = Gender.Unspecified,
    val licenseNumber: String = "",
    val verified: Boolean = false,
    val active: Boolean = false,
    val mainDiscipline: Discipline = Discipline.PSYCHOLOGY,
    val cvUrl: String? = null,
    val licenseUrl: String? = null,
    val speciality: String = "",
    val approach: String? = null,
    val topics: String = "",
    val expertiz: String = "",
    val modalities: Set<Modality> = emptySet(),
    val sessionTypes: Set<Sessions> = emptySet(),
    val populations: Set<Population> = emptySet(),
    val schedule: Map<Int, List<Int>> = emptyMap(),
    val semblance: String = "",
    val createdAt: Long? = null
) : UserProfile
