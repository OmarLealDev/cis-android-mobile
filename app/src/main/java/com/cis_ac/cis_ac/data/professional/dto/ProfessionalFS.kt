package com.cis_ac.cis_ac.data.professional.dto

data class ProfessionalFS(
    var uid: String? = null,
    var email: String? = null,
    var role: String? = null,

    var fullName: String? = null,
    var phone: String? = null,
    var dob: String? = null,
    var gender: String? = null,
    var licenseNumber: String? = null,
    var verified: Boolean? = null,
    var active: Boolean? = null,
    var mainDiscipline: String? = null,

    var cvUrl: String? = null,
    var licenseUrl: String? = null,

    var speciality: String? = null,
    var approach: String? = null,
    var topics: String? = null,
    var expertiz: String? = null,

    var modalities: List<String>? = null,
    var sessionTypes: List<String>? = null,
    var populations: List<String>? = null,

    var schedule: Map<String, List<Long>>? = null,

    var semblance: String? = null,
    var createdAt: Long? = null
)