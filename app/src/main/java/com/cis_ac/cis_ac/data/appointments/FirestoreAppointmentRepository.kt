package com.cis_ac.cis_ac.data.appointments

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.Appointment
import com.cis_ac.cis_ac.core.model.Discipline
import com.cis_ac.cis_ac.data.appointments.dto.AppointmentFS
import com.cis_ac.cis_ac.data.userprofile.dto.ProfessionalFS
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class FirestoreAppointmentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AppointmentRepository {

    private val pros = db.collection("professionals")
    private val appts = db.collection("appointments")

    override suspend fun getById(appointmentId: String): Result<Appointment> {
        return try {
            val snap = appts.document(appointmentId).get().await()
            if (!snap.exists()) {
                Result.Error("No existe la cita")
            } else {
                val dto = snap.toObject(AppointmentFS::class.java)
                val domain = dto?.toDomain()
                if (domain == null) {
                    Result.Error("Cita inválida")
                } else {
                    Result.Success(domain)
                }
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error obteniendo cita", e)
        }
    }


    override suspend fun listProfessionalsByDiscipline(
        discipline: Discipline
    ): Result<List<MinimalProfessional>> = try {
        val snap = pros.whereEqualTo("verified", true)
            .whereEqualTo("active", true)
            .whereEqualTo("mainDiscipline", discipline.name)
            .get().await()

        val list = snap.documents.mapNotNull { d ->
            d.toObject<ProfessionalFS>()?.let { p ->
                val uid = p.uid ?: return@let null
                val name = p.fullName ?: return@let null
                MinimalProfessional(uid = uid, fullName = name, mainDiscipline = discipline)
            }
        }
        Result.Success(list)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Error listando profesionales", e)
    }

    override suspend fun getProfessionalSchedule(proId: String): Result<Map<Int, List<Int>>> = try {
        val snap = pros.document(proId).get().await()
        val dto = snap.toObject<ProfessionalFS>() ?: return Result.Success(emptyMap())

        val map = dto.schedule?.mapNotNull { (k, v) ->
            val day = k.toIntOrNull() ?: return@mapNotNull null
            day to v.map { it.toInt() }.sorted()
        }?.toMap() ?: emptyMap()

        Result.Success(map)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Error cargando disponibilidad", e)
    }

    override suspend fun getBookedHours(
        proId: String,
        dateEpochDay: Long
    ): Result<Set<Int>> = try {
        val snap = appts
            .whereEqualTo("professionalId", proId)
            .whereEqualTo("dateEpochDay", dateEpochDay)
            .whereEqualTo("active", true)
            .get().await()

        val hours = snap.documents.mapNotNull { d ->
            d.toObject(AppointmentFS::class.java)?.hour24?.toInt()
        }.toSet()
        Result.Success(hours)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Error consultando horas ocupadas", e)
    }

    override suspend fun checkAvailability(
        proId: String,
        dateEpochDay: Long,
        hour24: Int
    ): Result<Boolean> = try {
        val snap = appts
            .whereEqualTo("professionalId", proId)
            .whereEqualTo("dateEpochDay", dateEpochDay)
            .whereEqualTo("hour24", hour24.toLong())
            .limit(1)
            .get().await()
        Result.Success(snap.isEmpty)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Error verificando disponibilidad", e)
    }

    override suspend fun confirm(appointmentId: String, confirmed: Boolean): Result<Unit> = try {
        appts.document(appointmentId).update(mapOf(
            "confirmed" to confirmed,
            "active" to true
        )).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "No se pudo actualizar confirmación", e)
    }

    override suspend fun cancel(appointmentId: String, reason: String?): Result<Unit> = try {
        val updates = hashMapOf<String, Any>(
            "active" to false,
            "confirmed" to false
        ).apply {
            if (reason != null) put("cancelReason", reason)
        }
        appts.document(appointmentId).update(updates).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "No se pudo cancelar la cita", e)
    }

    override suspend fun listPatientAppointments(patientId: String): Result<List<Appointment>> = try {
        val snap = appts
            .whereEqualTo("patientId", patientId)
            .get().await()
        val list = snap.documents.mapNotNull { d ->
            d.toObject(AppointmentFS::class.java)?.toDomain()
        }
        Result.Success(list)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Error listando citas del paciente", e)
    }

    override suspend fun listProfessionalAppointments(
        proId: String,
        dateEpochDay: Long?
    ): Result<List<Appointment>> = try {
        var q = appts
            .whereEqualTo("professionalId", proId)
            .whereEqualTo("active", true)
        if (dateEpochDay != null) q = q.whereEqualTo("dateEpochDay", dateEpochDay)
        val snap = q.get().await()
        val list = snap.documents.mapNotNull { d ->
            d.toObject(AppointmentFS::class.java)?.toDomain()
        }
        Result.Success(list)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Error listando citas del profesional", e)
    }

    override suspend fun create(appointment: Appointment): Result<String> = try {
        val deterministicId = "${appointment.professionalId}_${appointment.dateEpochDay}_${appointment.hour24}"
        val doc = appts.document(deterministicId)

        val fs = AppointmentFS(
            id = deterministicId,
            patientId = appointment.patientId,
            professionalId = appointment.professionalId,
            discipline = appointment.discipline.name,
            dateEpochDay = appointment.dateEpochDay,
            hour24 = appointment.hour24.toLong(),
            notes = appointment.notes,
            active = appointment.active,
            confirmed = appointment.confirmed,
            createdAt = appointment.createdAt
        )

        db.runTransaction { tx ->
            val existing = tx.get(doc)
            if (existing.exists()) {
                throw IllegalStateException("La hora ya está ocupada")
            }
            tx.set(doc, fs)
            true
        }.await()

        Result.Success(deterministicId)
    } catch (e: Exception) {
        Result.Error(e.message ?: "No se pudo crear la cita", e)
    }

    suspend fun getPatientNames(ids: Set<String>): Map<String, String> {
        return try {
            if (ids.isEmpty()) return emptyMap()

            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val col = db.collection("patients")

            val tasks = ids.map { id -> col.document(id).get() }
            val snaps = com.google.android.gms.tasks.Tasks.whenAllSuccess<com.google.firebase.firestore.DocumentSnapshot>(tasks).await()

            snaps.mapNotNull { snap ->
                val id = snap.id
                val name = snap.getString("fullName")
                if (name != null) id to name else null
            }.toMap()
        } catch (_: Exception) { emptyMap() }
    }


}


private fun AppointmentFS.toDomain(): Appointment? {
    val id = this.id ?: return null
    val professionalId = this.professionalId ?: return null
    val dateEpochDay = this.dateEpochDay ?: return null
    val hour24 = (this.hour24 ?: 0L).toInt()

    val disciplineEnum = runCatching {
        com.cis_ac.cis_ac.core.model.Discipline.valueOf(this.discipline ?: "")
    }.getOrElse { com.cis_ac.cis_ac.core.model.Discipline.PSYCHOLOGY }

    return Appointment(
        id = id,
        patientId = this.patientId,
        professionalId = professionalId,
        discipline = disciplineEnum,
        dateEpochDay = dateEpochDay,
        hour24 = hour24,
        notes = this.notes.orEmpty(),
        active = this.active ?: true,
        confirmed = this.confirmed ?: false,
        createdAt = this.createdAt ?: System.currentTimeMillis()
    )
}

