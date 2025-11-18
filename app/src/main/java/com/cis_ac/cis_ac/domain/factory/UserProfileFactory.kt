package com.cis_ac.cis_ac.domain.factory

import com.cis_ac.cis_ac.core.model.*
import com.cis_ac.cis_ac.core.model.modalityId
import com.cis_ac.cis_ac.core.model.sessionsId
import com.cis_ac.cis_ac.core.model.populationId

object UserProfileFactory {

    fun createUserProfile(
        uid: String,
        email: String,
        role: UserRole,
        details: Map<String, Any?>
    ): UserProfile? {
        return when (role) {
            UserRole.PATIENT       -> createPatient(uid, email, details)
            UserRole.PROFESSIONAL  -> createProfessional(uid, email, details)
            UserRole.ADMIN,
            UserRole.UNDEFINED     -> null
        }
    }

    private fun createPatient(
        uid: String,
        email: String,
        details: Map<String, Any?>
    ): Patient {
        val genderVal: Gender = when (val g = details["gender"]) {
            is Gender -> g
            is String -> runCatching { Gender.valueOf(g) }.getOrDefault(Gender.Unspecified)
            else -> Gender.Unspecified
        }

        return Patient(
            uid = uid,
            email = email,
            role = UserRole.PATIENT,
            fullName = (details["fullName"] as? String).orEmpty(),
            phone = (details["phone"] as? String).orEmpty(),
            dob = (details["dob"] as? String).orEmpty(),
            gender = genderVal
        )
    }

    private fun createProfessional(
        uid: String,
        email: String,
        details: Map<String, Any?>
    ): Professional {
        val discipline: Discipline = when (val raw = details["mainDiscipline"]) {
            is Discipline -> raw
            is String -> runCatching { Discipline.valueOf(raw) }.getOrDefault(Discipline.PSYCHOLOGY)
            else -> Discipline.PSYCHOLOGY
        }

        val schedule: Map<Int, List<Int>> = parseSchedule(details["schedule"])

        val modalitiesSet: Set<Modality>     = parseModalitySet(details["modalities"])
        val sessionsRaw = details["sessions"] ?: details["sessionTypes"] ?: details["session_ids"]
        val sessionsSet: Set<Sessions> = parseSessionsSet(sessionsRaw)
        val populationsSet: Set<Population>  = parsePopulationSet(details["populations"])

        val genderVal = when (val g = details["gender"]) {
            is Gender -> g
            is String -> runCatching { Gender.valueOf(g) }.getOrDefault(Gender.Unspecified)
            else -> Gender.Unspecified
        }

        return Professional(
            uid = uid,
            email = email,
            role = UserRole.PROFESSIONAL,
            fullName = (details["fullName"] as? String).orEmpty(),
            phone = (details["phone"] as? String).orEmpty(),
            dob = (details["dob"] as? String).orEmpty(),
            gender = genderVal,
            licenseNumber = (details["licenseNumber"] as? String).orEmpty(),
            mainDiscipline = discipline,
            verified = (details["verified"] as? Boolean) ?: false,
            active = (details["active"] as? Boolean) ?: false,
            cvUrl = details["cvUrl"] as? String,
            licenseUrl = details["licenseUrl"] as? String,
            speciality = (details["speciality"] as? String).orEmpty(),
            approach = details["approach"] as? String,
            topics = (details["topics"] as? String).orEmpty(),
            expertiz = (details["expertiz"] as? String).orEmpty(),
            modalities = modalitiesSet,
            sessionTypes = sessionsSet,
            populations = populationsSet,
            schedule = schedule,
            semblance = (details["semblance"] as? String).orEmpty(),
            createdAt = details["createdAt"] as? Long
        )
    }


    private fun toIntOrNull(x: Any?): Int? = when (x) {
        is Number -> x.toInt()
        is String -> x.toIntOrNull()
        else -> null
    }

    private fun parseSchedule(raw: Any?): Map<Int, List<Int>> = when (raw) {
        is Map<*, *> -> raw.mapNotNull { (k, v) ->
            val day = when (k) {
                is Number -> k.toInt()
                is String -> k.toIntOrNull()
                else -> null
            } ?: return@mapNotNull null

            val hours: List<Int> = when (v) {
                is List<*> -> v.mapNotNull {
                    when (it) {
                        is Number -> it.toInt()
                        is String -> it.toIntOrNull()
                        else -> null
                    }
                }
                else -> emptyList()
            }
            day to hours.sorted()
        }.toMap()
        else -> emptyMap()
    }

    private fun parseModalitySet(raw: Any?): Set<Modality> =
        parseIntSet(raw).map { Modality.fromInt(it) }.toSet()

    private fun parseSessionsSet(raw: Any?): Set<Sessions> =
        parseIntSet(raw).map { Sessions.fromInt(it) }.toSet()

    private fun parsePopulationSet(raw: Any?): Set<Population> =
        parseIntSet(raw).map { Population.fromInt(it) }.toSet()

    private fun parseIntSet(raw: Any?): Set<Int> = when (raw) {
        is Set<*>  -> raw.mapNotNull { asInt(it) }.toSet()
        is List<*> -> raw.mapNotNull { asInt(it) }.toSet()
        is Number  -> setOf(raw.toInt())
        is String  -> raw.split(',', ' ', ';').mapNotNull { it.toIntOrNull() }.toSet()
        else       -> emptySet()
    }

    private fun asInt(x: Any?): Int? = when (x) {
        is Number     -> x.toInt()
        is String     -> x.toIntOrNull()
        is Modality   -> modalityId(x)
        is Sessions   -> sessionsId(x)
        is Population -> populationId(x)
        else -> null
    }

    fun getUserCollection(role: UserRole): String = when (role) {
        UserRole.PATIENT       -> "patients"
        UserRole.PROFESSIONAL  -> "professionals"
        UserRole.ADMIN         -> "admins"
        UserRole.UNDEFINED     -> "undefined"
    }
}
