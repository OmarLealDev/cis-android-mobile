package com.cis_ac.cis_ac.data.admin

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.Discipline
import com.cis_ac.cis_ac.core.model.Gender
import com.cis_ac.cis_ac.core.model.Modality
import com.cis_ac.cis_ac.core.model.Population
import com.cis_ac.cis_ac.core.model.Professional
import com.cis_ac.cis_ac.core.model.Sessions
import com.cis_ac.cis_ac.core.model.UserRole
import com.cis_ac.cis_ac.data.userprofile.dto.ProfessionalFS
import com.google.firebase.firestore.FieldValue // ✅
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreAdminRequestsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AdminRequestsRepository {

    private val pros = db.collection("professionals")

    override fun watchPendingProfessionals(): Flow<Result<List<Professional>>> = callbackFlow {
        trySend(Result.Loading)

        val reg = pros
            .whereEqualTo("active", true)     // ✅
            .whereEqualTo("verified", false)  // ✅
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(Result.Error(err.message ?: "Error al cargar solicitudes", err))
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { d ->
                    d.toObject<ProfessionalFS>()?.toDomain()
                }.orEmpty()
                trySend(Result.Success(list))
            }

        awaitClose { reg.remove() }
    }

    override suspend fun getProfessional(uid: String): Result<Professional> = try {
        val snap = pros.document(uid).get().await()
        val dto = snap.toObject<ProfessionalFS>()
        val pro = dto?.toDomain() ?: return Result.Error("No se encontró la solicitud")
        Result.Success(pro)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Error al obtener solicitud", e)
    }

    override suspend fun approve(uid: String): Result<Unit> = try {
        pros.document(uid).update(
            mapOf(
                "verified"   to true,
                "active"     to true,
                "verifiedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Error al aprobar", e)
    }

    override suspend fun reject(uid: String): Result<Unit> = try {
        pros.document(uid).update(
            mapOf(
                "verified" to false,
                "active"   to false
            )
        ).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Error al rechazar", e)
    }

    private fun ProfessionalFS.toDomain(): Professional? {
        val uid = this.uid ?: return null
        val email = this.email ?: return null

        val roleEnum = runCatching { UserRole.valueOf(this.role ?: "UNDEFINED") }
            .getOrDefault(UserRole.UNDEFINED)

        val genderEnum = runCatching { Gender.valueOf(this.gender ?: Gender.Unspecified.name) }
            .getOrDefault(Gender.Unspecified)

        val disciplineEnum = runCatching {
            Discipline.valueOf(this.mainDiscipline ?: Discipline.PSYCHOLOGY.name)
        }.getOrDefault(Discipline.PSYCHOLOGY)

        val topicsStr = this.topics ?: ""

        val modalitiesSet: Set<Modality> =
            (this.modalities ?: emptyList()).map { i: Int -> Modality.fromInt(i) }.toSet()
        val sessionsSet: Set<Sessions> =
            (this.sessionTypes ?: emptyList()).map { i: Int -> Sessions.fromInt(i) }.toSet()
        val populationsSet: Set<Population> =
            (this.populations ?: emptyList()).map { i: Int -> Population.fromInt(i) }.toSet()

        val scheduleIntMap: Map<Int, List<Int>> = when {
            this.scheduleInt != null -> this.scheduleInt!!
            this.schedule != null -> this.schedule!!.mapNotNull { (k, v) ->
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
            topics     = topicsStr,
            expertiz   = this.expertiz.orEmpty(),

            modalities   = modalitiesSet,
            sessionTypes = sessionsSet,
            populations  = populationsSet,

            schedule  = scheduleIntMap,
            semblance = this.semblance.orEmpty(),
            createdAt = this.createdAt
        )
    }
}
