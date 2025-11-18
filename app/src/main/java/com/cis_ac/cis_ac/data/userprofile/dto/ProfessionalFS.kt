package com.cis_ac.cis_ac.data.userprofile.dto

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
    var semblance: String? = null,

    var modalities: List<Int>? = null,
    var sessionTypes: List<Int>? = null,
    var populations: List<Int>? = null,

    var schedule: Map<String, List<Long>>? = null,
    @get:com.google.firebase.firestore.Exclude @set:com.google.firebase.firestore.Exclude
    var scheduleInt: Map<Int, List<Int>>? = null,

    var createdAt: Long? = null
)
