package com.cis_ac.cis_ac.data.home

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.data.appointments.AppointmentRepository
import com.cis_ac.cis_ac.data.appointments.FirestoreAppointmentRepository
import com.cis_ac.cis_ac.ui.feature.home.patient.NextAppointment
import com.cis_ac.cis_ac.ui.feature.home.patient.PatientProfile
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

class FirestorePatientHomeRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val apptRepo: AppointmentRepository = FirestoreAppointmentRepository()
) : PatientHomeRepository {

    private val tz: ZoneId = runCatching { ZoneId.of("America/Mazatlan") }
        .getOrElse { ZoneId.systemDefault() }

    override suspend fun loadProfile(): PatientProfile = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext PatientProfile("Paciente")
        val snap = db.collection("patients").document(uid).get().await()
        val fullName = (snap.getString("fullName") ?: "").ifBlank { "Paciente" }
        PatientProfile(displayName = fullName)
    }

    override suspend fun loadNextAppointmentOrNull(): NextAppointment? = withContext(Dispatchers.IO) {
        val patientId = auth.currentUser?.uid ?: return@withContext null

        when (val res = apptRepo.listPatientAppointments(patientId)) {
            is Result.Success -> {
                val all = res.data.orEmpty().filter { it.active != false }
                if (all.isEmpty()) return@withContext null

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
                if (upcoming.isEmpty()) return@withContext null

                val next = upcoming.minByOrNull { a ->
                    (a.dateEpochDay.toLong() shl 5) + (a.hour24.toLong() and 31)
                } ?: return@withContext null

                val date = LocalDate.ofEpochDay(next.dateEpochDay.toLong())
                val dt = LocalDateTime.of(date, LocalTime.of(next.hour24, 0))

                val proId = next.professionalId ?: return@withContext null
                val proName = fetchProfessionalName(proId) ?: "Profesional"

                val (disciplineWordFem, prefix) = when (next.discipline.name) {
                    "PSYCHOLOGY"     -> "Psicológica" to "Psic."
                    "NUTRITION"      -> "Nutricional" to "L.N."
                    "PHYSIOTHERAPY"  -> "Fisioterapéutica" to "L.FT."
                    else             -> "Psicológica" to "Psic."
                }

                NextAppointment(
                    title = "Consulta $disciplineWordFem",
                    professionalName = "$prefix $proName",
                    dateTime = dt
                )
            }
            else -> null
        }
    }

    private suspend fun fetchProfessionalName(id: String): String? {
        val snap = db.collection("professionals").document(id).get().await()
        return snap.getString("fullName")
    }
}
