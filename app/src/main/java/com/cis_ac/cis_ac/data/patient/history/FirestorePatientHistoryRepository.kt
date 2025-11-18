package com.cis_ac.cis_ac.data.patient.history

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.UserRole
import com.cis_ac.cis_ac.core.model.history.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Locale

class FirestorePatientHistoryRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : PatientHistoryRepository {

    private fun profileDoc(patientId: String) =
        db.collection("patients").document(patientId)
            .collection("meta").document("clinicalProfile")

    private fun historyCol(patientId: String) =
        db.collection("patients").document(patientId).collection("history")


    private data class GeneralInfoFS(
        val ageYears: Int? = null,
        val weightKg: Double? = null,
        val heightCm: Double? = null,
        val bloodType: String? = null,
        val chronicConditions: List<String> = emptyList(),
        val surgeries: List<String> = emptyList(),
        val habits: Map<String, Any?> = emptyMap(),
        val emergencyContact: Map<String, Any?> = emptyMap(),
        val updatedAt: Timestamp? = null,
        val updatedBy: Map<String, Any?>? = null
    )

    private data class HistoryEntryFS(
        val section: String = "",
        val date: String = "",
        val text: String = "",
        val createdBy: Map<String, Any?> = emptyMap(),
        val createdAt: Timestamp? = null,
        val updatedBy: Map<String, Any?>? = null,
        val updatedAt: Timestamp? = null
    )

    private fun UserRef.toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "name" to name,
        "role" to role.name
    )

    private fun mapToUserRef(map: Map<String, Any?>?): UserRef? {
        map ?: return null
        val roleStr = (map["role"] as? String)
            ?.trim()
            ?.uppercase(
                Locale.ROOT)
            ?: UserRole.PATIENT.name

        val roleEnum = try {
            UserRole.valueOf(roleStr)
        } catch (_: IllegalArgumentException) {
            UserRole.PATIENT
        }

        return UserRef(
            uid = map["uid"] as? String ?: "",
            name = map["name"] as? String ?: "",
            role = roleEnum
        )
    }

    private fun GeneralInfoFS.toDomain(): GeneralInfo = GeneralInfo(
        ageYears = ageYears,
        weightKg = weightKg?.toFloat(),
        heightCm = heightCm?.toFloat(),
        bloodType = bloodType,
        chronicConditions = chronicConditions,
        surgeries = surgeries,
        habits = Habits(
            smoker = (habits["smoker"] as? Boolean) ?: false,
            alcoholUse = (habits["alcoholUse"] as? Boolean) ?: false,
            exerciseFreq = (habits["exerciseFreq"] as? String).orEmpty()
        ),
        emergencyContact = EmergencyContact(
            name = (emergencyContact["name"] as? String).orEmpty(),
            phone = (emergencyContact["phone"] as? String).orEmpty(),
            relation = (emergencyContact["relation"] as? String).orEmpty()
        ),
        updatedAt = updatedAt?.toDate()?.time,
        updatedBy = mapToUserRef(updatedBy)
    )

    private fun HistoryEntryFS.toDomain(id: String): HistoryEntry = HistoryEntry(
        id = id,
        section = section,
        date = date,
        text = text,
        createdBy = mapToUserRef(createdBy) ?: UserRef(),
        createdAt = createdAt?.toDate()?.time,
        updatedBy = mapToUserRef(updatedBy),
        updatedAt = updatedAt?.toDate()?.time
    )


    override suspend fun loadGeneral(patientId: String): Result<GeneralInfo> = try {
        val snap = profileDoc(patientId).get().await()
        if (!snap.exists()) {
            Result.Success(GeneralInfo())
        } else {
            val fs = snap.toObject(GeneralInfoFS::class.java) ?: GeneralInfoFS()
            Result.Success(fs.toDomain())
        }
    } catch (e: Exception) {
        Result.Error(e.localizedMessage ?: "Error al cargar datos generales")
    }

    override suspend fun saveGeneral(patientId: String, info: GeneralInfo, actor: UserRef): Result<Unit> = try {
        val data = hashMapOf<String, Any?>(
            "ageYears" to info.ageYears,
            "weightKg" to info.weightKg?.toDouble(),
            "heightCm" to info.heightCm?.toDouble(),
            "bloodType" to info.bloodType,
            "chronicConditions" to info.chronicConditions,
            "surgeries" to info.surgeries,
            "habits" to mapOf(
                "smoker" to info.habits.smoker,
                "alcoholUse" to info.habits.alcoholUse,
                "exerciseFreq" to info.habits.exerciseFreq
            ),
            "emergencyContact" to mapOf(
                "name" to info.emergencyContact.name,
                "phone" to info.emergencyContact.phone,
                "relation" to info.emergencyContact.relation
            ),
            "updatedAt" to FieldValue.serverTimestamp(),
            "updatedBy" to actor.toMap()
        )
        profileDoc(patientId).set(data).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.localizedMessage ?: "No se pudieron guardar los datos generales")
    }


    override suspend fun listNotes(patientId: String, section: String): Result<List<HistoryEntry>> = try {
        val qs = historyCol(patientId)
            .whereEqualTo("section", section)
            .get()
            .await()

        val list = qs.documents.map { it.toHistoryEntryDomain() }
            .sortedBy { it.createdAt ?: 0L } // ordena en cliente; evita índice

        Result.Success(list)
    } catch (e: Exception) {
        Result.Error(e.localizedMessage ?: "Error al cargar notas")
    }


    override suspend fun addNote(
        patientId: String,
        section: String,
        date: String,
        text: String,
        actor: UserRef
    ): Result<String> = try {
        val data = hashMapOf(
            "section" to section,
            "date" to date,
            "text" to text,
            "description" to text,
            "createdBy" to actor.toMap(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedBy" to null,
            "updatedAt" to null
        )
        val ref = historyCol(patientId).add(data).await()
        Result.Success(ref.id)
    } catch (e: Exception) {
        Result.Error(e.localizedMessage ?: "No se pudo agregar la nota")
    }

    override suspend fun updateNote(
        patientId: String,
        noteId: String,
        date: String,
        text: String,
        actor: UserRef
    ): Result<Unit> = try {
        val data = hashMapOf<String, Any?>(
            "date" to date,
            "text" to text,
            "description" to text,
            "updatedBy" to actor.toMap(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        historyCol(patientId).document(noteId).update(data).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.localizedMessage ?: "No se pudo actualizar la nota")
    }
    private fun com.google.firebase.firestore.DocumentSnapshot.toHistoryEntryDomain(): HistoryEntry {
        val data = this.data ?: emptyMap<String, Any?>()

        val section = data["section"] as? String ?: ""
        val date    = data["date"] as? String ?: ""

        val text = when {
            data["text"] is String        -> data["text"] as String
            data["description"] is String -> data["description"] as String
            data["note"] is String        -> data["note"] as String
            else -> ""
        }

        val createdByMap = data["createdBy"] as? Map<String, Any?>
        val updatedByMap = data["updatedBy"] as? Map<String, Any?>

        val createdAtTs = data["createdAt"] as? com.google.firebase.Timestamp
        val updatedAtTs = data["updatedAt"] as? com.google.firebase.Timestamp

        return HistoryEntry(
            id = id,
            section = section,
            date = date,
            text = text,
            createdBy = mapToUserRef(createdByMap) ?: UserRef(),
            createdAt = createdAtTs?.toDate()?.time,
            updatedBy = mapToUserRef(updatedByMap),
            updatedAt = updatedAtTs?.toDate()?.time
        )
    }

}
