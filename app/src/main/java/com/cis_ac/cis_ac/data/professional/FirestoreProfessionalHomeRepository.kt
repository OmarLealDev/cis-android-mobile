package com.cis_ac.cis_ac.data.professional

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.data.appointments.AppointmentRepository
import com.cis_ac.cis_ac.data.appointments.FirestoreAppointmentRepository
import com.cis_ac.cis_ac.ui.feature.home.professional.ProNextAppointmentItem
import com.cis_ac.cis_ac.ui.feature.home.professional.ProfessionalProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class FirestoreProfessionalHomeRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val apptRepo: AppointmentRepository = FirestoreAppointmentRepository()
) : ProfessionalHomeRepository {

    private val tz: ZoneId = runCatching { ZoneId.of("America/Mazatlan") }
        .getOrElse { ZoneId.systemDefault() }

    override suspend fun loadProfile(): ProfessionalProfile = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext ProfessionalProfile("Profesional")
        val snap = db.collection("professionals").document(uid).get().await()
        val name = (snap.getString("fullName") ?: "").ifBlank { "Profesional" }
        ProfessionalProfile(displayName = name)
    }

    override suspend fun loadNextAppointments(): List<ProNextAppointmentItem> = withContext(Dispatchers.IO) {
        val proId = auth.currentUser?.uid ?: return@withContext emptyList()

        when (val res = apptRepo.listProfessionalAppointments(proId)) {
            is Result.Success -> {
                val all = res.data.orEmpty().filter { it.active != false }
                if (all.isEmpty()) return@withContext emptyList()

                val now = LocalDateTime.now(tz)
                val todayEpochDay = now.toLocalDate().toEpochDay().toInt()
                val nowHour = now.hour

                val upcoming = all.filter { a ->
                    when {
                        a.dateEpochDay > todayEpochDay -> true
                        a.dateEpochDay < todayEpochDay -> false
                        else -> a.hour24 >= nowHour
                    }
                }
                if (upcoming.isEmpty()) return@withContext emptyList()

                val patientIds = upcoming.mapNotNull { it.patientId }.toSet()
                val patientNames = fetchPatientNames(patientIds)

                upcoming.mapNotNull { a ->
                    val date = LocalDate.ofEpochDay(a.dateEpochDay.toLong())
                    val ldt  = LocalDateTime.of(date, LocalTime.of(a.hour24, 0))
                    val whenMs = ldt.atZone(tz).toInstant().toEpochMilli()

                    val patientId   = a.patientId ?: return@mapNotNull null
                    val patientName = patientNames[patientId] ?: "Paciente"

                    val disciplineLabel = a.discipline.name

                    ProNextAppointmentItem(
                        id = a.id ?: "${patientId}_${a.dateEpochDay}_${a.hour24}",
                        patientId = patientId,
                        patientName = patientName,
                        dateTimeMillis = whenMs,
                        confirmed = a.confirmed,
                        disciplineLabel = disciplineLabel,

                    )
                }
                    .sortedBy { it.dateTimeMillis }
                    .take(2)
            }
            else -> emptyList()
        }
    }

    private suspend fun fetchPatientNames(ids: Set<String>): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()
        val out = mutableMapOf<String, String>()
        ids.chunked(10).forEach { batch ->
            val snap = db.collection("patients")
                .whereIn(FieldPath.documentId(), batch.toList())
                .get()
                .await()
            snap.documents.forEach { d ->
                out[d.id] = d.getString("fullName") ?: "Paciente"
            }
        }
        return out
    }
}
