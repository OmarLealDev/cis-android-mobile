package com.cis_ac.cis_ac.data.professional

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.*
import com.cis_ac.cis_ac.data.userprofile.dto.ProfessionalFS
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

import com.cis_ac.cis_ac.core.model.modalitiesToIdList
import com.cis_ac.cis_ac.core.model.sessionsToIdList
import com.cis_ac.cis_ac.core.model.populationsToIdList
import com.cis_ac.cis_ac.core.model.idsToModalities
import com.cis_ac.cis_ac.core.model.idsToSessions
import com.cis_ac.cis_ac.core.model.idsToPopulations
class FirestoreProfessionalProfileRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProfessionalProfileRepository {

    private val col = db.collection("professionals")

    override suspend fun getProfessional(uid: String): Result<Professional> = try {
        val snap = col.document(uid).get().await()
        val dto = snap.toObject<ProfessionalFS>() ?: return Result.Error("No se encontró el profesional")
        val pro = dto.toDomain() ?: return Result.Error("Perfil inválido")
        Result.Success(pro)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Error cargando perfil", e)
    }

    override suspend fun updateProfessional(uid: String, patch: ProfessionalUpdate): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>()

            patch.fullName?.let { updates["fullName"] = it }
            patch.phone?.let { updates["phone"] = it }
            patch.dob?.let { updates["dob"] = it }
            patch.gender?.let { updates["gender"] = it.name }
            patch.licenseNumber?.let { updates["licenseNumber"] = it }
            patch.speciality?.let { updates["speciality"] = it }
            patch.approach?.let { updates["approach"] = it }
            patch.topics?.let { updates["topics"] = it }
            patch.expertiz?.let { updates["expertiz"] = it }
            patch.semblance?.let { updates["semblance"] = it }

            patch.modalities?.let   { updates["modalities"]   = modalitiesToIdList(it) }
            patch.sessionTypes?.let { updates["sessionTypes"] = sessionsToIdList(it) }
            patch.populations?.let  { updates["populations"]  = populationsToIdList(it) }

            if (updates.isEmpty()) return Result.Success(Unit)

            col.document(uid).update(updates as Map<String, Any>).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "No se pudo actualizar el perfil", e)
        }
    }
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
