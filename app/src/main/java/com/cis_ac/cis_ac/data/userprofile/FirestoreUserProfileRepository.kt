package com.cis_ac.cis_ac.data.userprofile

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.*
import com.cis_ac.cis_ac.data.userprofile.dto.PatientFS
import com.cis_ac.cis_ac.data.userprofile.dto.ProfessionalFS
import com.cis_ac.cis_ac.domain.factory.UserProfileFactory
import com.cis_ac.cis_ac.core.model.Admin
import com.cis_ac.cis_ac.data.userprofile.dto.AdminFS
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import com.cis_ac.cis_ac.core.model.modalityId
import com.cis_ac.cis_ac.core.model.sessionsId
import com.cis_ac.cis_ac.core.model.populationId

import com.cis_ac.cis_ac.core.model.idsToModalities
import com.cis_ac.cis_ac.core.model.idsToSessions
import com.cis_ac.cis_ac.core.model.idsToPopulations

class FirestoreUserProfileRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserProfileRepository {

    override suspend fun createUserProfile(userProfile: UserProfile): Result<String> = try {
        val collectionPath = UserProfileFactory.getUserCollection(userProfile.role)
        val doc = firestore.collection(collectionPath).document(userProfile.uid)

        when (userProfile) {
            is Patient -> doc.set(userProfile).await()

            is Professional -> {
                val scheduleFS: Map<String, List<Long>> =
                    userProfile.schedule.entries.associate { (dayInt, hours) ->
                        dayInt.toString() to hours.map { it.toLong() }
                    }

                val fs = ProfessionalFS(
                    uid = userProfile.uid,
                    email = userProfile.email,
                    role = userProfile.role.name,
                    fullName = userProfile.fullName,
                    phone = userProfile.phone,
                    dob = userProfile.dob,
                    gender = userProfile.gender.name,
                    licenseNumber = userProfile.licenseNumber,
                    verified = userProfile.verified,
                    active = userProfile.active,
                    mainDiscipline = userProfile.mainDiscipline.name,
                    cvUrl = userProfile.cvUrl,
                    licenseUrl = userProfile.licenseUrl,

                    speciality = userProfile.speciality,
                    approach   = userProfile.approach,
                    topics     = userProfile.topics,
                    expertiz   = userProfile.expertiz,
                    semblance  = userProfile.semblance,

                    modalities   = userProfile.modalities.map { modalityId(it) },
                    sessionTypes = userProfile.sessionTypes.map { sessionsId(it) },
                    populations  = userProfile.populations.map { populationId(it) },

                    schedule   = scheduleFS,
                    createdAt  = userProfile.createdAt
                )
                doc.set(fs).await()
            }

            is Admin -> doc.set(userProfile).await()
            else -> doc.set(userProfile).await()
        }
        Result.Success(userProfile.uid)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to create user profile", e)
    }

    override suspend fun getUserProfile(uid: String, role: UserRole): Result<UserProfile> {
        return try {
            val collectionPath = UserProfileFactory.getUserCollection(role)
            val snap = firestore.collection(collectionPath).document(uid).get().await()
            if (!snap.exists()) return Result.Error("User profile not found for UID: $uid")

            when (role) {
                UserRole.PATIENT -> {
                    val dto = snap.toObject(PatientFS::class.java)
                    dto?.toDomain()?.let { Result.Success(it) } ?: Result.Error("Failed to parse patient profile")
                }
                UserRole.PROFESSIONAL -> {
                    val dto = snap.toObject(ProfessionalFS::class.java)
                    dto?.toDomain()?.let { Result.Success(it) } ?: Result.Error("Failed to parse professional profile")
                }
                UserRole.ADMIN -> {
                    val dto = snap.toObject(AdminFS::class.java)
                    dto?.toDomain()?.let { Result.Success(it) } ?: Result.Error("Failed to parse admin profile")
                }
                else -> Result.Error("Unsupported role: $role")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get user profile", e)
        }
    }

    override suspend fun getCurrentUserProfile(uid: String): Result<UserProfile> = try {
        firestore.collection("admins").document(uid).get().await().let { a ->
            if (a.exists()) a.toObject(AdminFS::class.java)?.toDomain()?.let { return Result.Success(it) }
        }
        firestore.collection("patients").document(uid).get().await().let { p ->
            if (p.exists()) p.toObject<PatientFS>()?.toDomain()?.let { return Result.Success(it) }
        }
        firestore.collection("professionals").document(uid).get().await().let { pr ->
            if (pr.exists()) pr.toObject<ProfessionalFS>()?.toDomain()?.let { return Result.Success(it) }
        }
        Result.Error("User profile not found for UID: $uid")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get current user profile", e)
    }
}


private fun PatientFS.toDomain(): Patient? {
    val uid = this.uid ?: return null
    val email = this.email ?: return null
    val roleEnum = runCatching { UserRole.valueOf(this.role ?: "UNDEFINED") }.getOrDefault(UserRole.UNDEFINED)
    val genderEnum = runCatching { Gender.valueOf(this.gender ?: "Unspecified") }.getOrDefault(Gender.Unspecified)

    return Patient(
        uid = uid,
        email = email,
        role = roleEnum,
        fullName = this.fullName.orEmpty(),
        phone = this.phone.orEmpty(),
        dob = this.dob.orEmpty(),
        gender = genderEnum
    )
}

private fun ProfessionalFS.toDomain(): Professional? {
    val uid = this.uid ?: return null
    val email = this.email ?: return null

    val roleEnum = runCatching { UserRole.valueOf(this.role ?: "UNDEFINED") }.getOrDefault(UserRole.UNDEFINED)
    val genderEnum = runCatching { Gender.valueOf(this.gender ?: Gender.Unspecified.name) }.getOrDefault(Gender.Unspecified)
    val disciplineEnum = runCatching { Discipline.valueOf(this.mainDiscipline ?: Discipline.PSYCHOLOGY.name) }
        .getOrDefault(Discipline.PSYCHOLOGY)

    val modalitiesSet  = idsToModalities(this.modalities ?: emptyList())
    val sessionsSet    = idsToSessions(this.sessionTypes ?: emptyList())
    val populationsSet = idsToPopulations(this.populations ?: emptyList())

    val scheduleIntMap: Map<Int, List<Int>> = when {
        this.scheduleInt != null -> this.scheduleInt!!
        this.schedule != null    -> this.schedule!!.mapNotNull { (k, v) ->
            val day = k.toIntOrNull() ?: return@mapNotNull null
            day to v.map { it.toInt() }
        }.toMap()
        else -> emptyMap()
    }

    return Professional(
        uid = uid,
        email = email,
        role = roleEnum,
        fullName = this.fullName.orEmpty(),
        phone = this.phone.orEmpty(),
        dob = this.dob.orEmpty(),
        gender = genderEnum,
        licenseNumber = this.licenseNumber.orEmpty(),
        verified = this.verified ?: false,
        active = this.active ?: false,
        mainDiscipline = disciplineEnum,
        cvUrl = this.cvUrl,
        licenseUrl = this.licenseUrl,

        speciality = this.speciality.orEmpty(),
        approach   = this.approach,
        topics     = this.topics.orEmpty(),
        expertiz   = this.expertiz.orEmpty(),

        modalities   = modalitiesSet,
        sessionTypes = sessionsSet,
        populations  = populationsSet,

        schedule  = scheduleIntMap,
        semblance = this.semblance.orEmpty(),
        createdAt = this.createdAt
    )
}

private fun AdminFS.toDomain(): Admin? {
    val uid = this.uid ?: return null
    val email = this.email ?: return null
    val roleEnum = runCatching { UserRole.valueOf(this.role ?: "ADMIN") }.getOrDefault(UserRole.ADMIN)
    return Admin(uid = uid, email = email, role = roleEnum, fullName = this.fullName.orEmpty())
}
