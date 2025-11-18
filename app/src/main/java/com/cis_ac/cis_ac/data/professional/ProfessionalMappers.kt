package com.cis_ac.cis_ac.data.professional

import com.cis_ac.cis_ac.core.model.*
import com.cis_ac.cis_ac.data.professional.dto.ProfessionalFS

fun ProfessionalFS.toDomain(): Professional? {
    val uid = uid ?: return null
    val email = email ?: return null

    val roleEnum = runCatching { UserRole.valueOf(role ?: "UNDEFINED") }
        .getOrElse { UserRole.UNDEFINED }
    val genderEnum = runCatching { Gender.valueOf(gender ?: Gender.Unspecified.name) }
        .getOrElse { Gender.Unspecified }
    val disciplineEnum = runCatching { Discipline.valueOf(mainDiscipline ?: Discipline.PSYCHOLOGY.name) }
        .getOrElse { Discipline.PSYCHOLOGY }

    val modalitiesSet = modalities.orEmpty().mapNotNull {
        runCatching { Modality.valueOf(it) }.getOrNull()
    }.toSet()
    val sessionsSet = sessionTypes.orEmpty().mapNotNull {
        runCatching { Sessions.valueOf(it) }.getOrNull()
    }.toSet()
    val populationsSet = populations.orEmpty().mapNotNull {
        runCatching { Population.valueOf(it) }.getOrNull()
    }.toSet()

    val scheduleInt: Map<Int, List<Int>> = schedule.orEmpty().mapNotNull { (k, v) ->
        val day = k.toIntOrNull() ?: return@mapNotNull null
        day to v.map { it.toInt() }.sorted()
    }.toMap()

    return Professional(
        uid = uid,
        email = email,
        role = roleEnum,
        fullName = fullName.orEmpty(),
        phone = phone.orEmpty(),
        dob = dob.orEmpty(),
        gender = genderEnum,
        licenseNumber = licenseNumber.orEmpty(),
        verified = verified ?: false,
        active = active ?: false,
        mainDiscipline = disciplineEnum,
        cvUrl = cvUrl,
        licenseUrl = licenseUrl,
        speciality = speciality.orEmpty(),
        approach = approach,
        topics = (topics ?: "").orEmpty(),
        expertiz = expertiz.orEmpty(),
        modalities = modalitiesSet,
        sessionTypes = sessionsSet,
        populations = populationsSet,
        schedule = scheduleInt,
        semblance = semblance.orEmpty(),
        createdAt = createdAt
    )
}

fun Professional.toFS(): ProfessionalFS = ProfessionalFS(
    uid = uid,
    email = email,
    role = role.name,

    fullName = fullName,
    phone = phone,
    dob = dob,
    gender = gender.name,
    licenseNumber = licenseNumber,
    verified = verified,
    active = active,
    mainDiscipline = mainDiscipline.name,

    cvUrl = cvUrl,
    licenseUrl = licenseUrl,

    speciality = speciality,
    approach = approach,
    topics = topics,
    expertiz = expertiz,

    modalities = modalities.map { it.name },
    sessionTypes = sessionTypes.map { it.name },
    populations = populations.map { it.name },

    schedule = schedule.map { (day, hours) ->
        day.toString() to hours.map { it.toLong() }
    }.toMap(),

    semblance = semblance,
    createdAt = createdAt
)
