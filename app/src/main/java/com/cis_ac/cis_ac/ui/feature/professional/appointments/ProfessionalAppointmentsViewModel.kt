package com.cis_ac.cis_ac.ui.feature.professional.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.data.appointments.AppointmentRepository
import com.cis_ac.cis_ac.data.appointments.FirestoreAppointmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class ProfessionalAppointmentsViewModel(
    private val repo: AppointmentRepository = FirestoreAppointmentRepository(),
    private val currentProfessionalIdProvider: () -> String = {
        FirebaseAuth.getInstance().currentUser?.uid ?: error("No hay usuario autenticado")
    }
) : ViewModel() {

    private val _ui = MutableStateFlow<ProfessionalAppointmentsUiState>(ProfessionalAppointmentsUiState.Loading)
    val ui: StateFlow<ProfessionalAppointmentsUiState> = _ui

    private val _showUpcoming = MutableStateFlow(false)
    private val _showPast     = MutableStateFlow(false)
    val showUpcoming: StateFlow<Boolean> = _showUpcoming
    val showPast: StateFlow<Boolean>     = _showPast

    fun toggleUpcoming() { _showUpcoming.value = !_showUpcoming.value }
    fun togglePast()     { _showPast.value     = !_showPast.value }

    fun load() = viewModelScope.launch {
        _ui.value = ProfessionalAppointmentsUiState.Loading

        val proId = runCatching { currentProfessionalIdProvider() }.getOrElse { t ->
            _ui.value = ProfessionalAppointmentsUiState.Error(t.message ?: "Falta ID del profesional")
            return@launch
        }

        when (val res = repo.listProfessionalAppointments(proId)) {
            is Result.Success -> {
                val appts = res.data.filter { it.active != false }

                val patientIds = appts.mapNotNull { it.patientId }.toSet()
                val names = fetchPatientNames(patientIds)

                val tz   = TimeZone.getTimeZone("America/Mazatlan")
                val cal  = Calendar.getInstance(tz)
                val y    = cal.get(Calendar.YEAR)
                val m    = cal.get(Calendar.MONTH) + 1
                val d    = cal.get(Calendar.DAY_OF_MONTH)
                val hour = cal.get(Calendar.HOUR_OF_DAY)

                val todayEpochDay = LocalDate.of(y, m, d).toEpochDay()

                val (upcomingList, pastList) = appts.partition { a ->
                    when {
                        a.dateEpochDay > todayEpochDay -> true
                        a.dateEpochDay < todayEpochDay -> false
                        else -> a.hour24 >= hour
                    }
                }

                val upcoming = upcomingList
                    .map { a ->
                        val date = LocalDate.ofEpochDay(a.dateEpochDay)
                        val dt   = LocalDateTime.of(date, LocalTime.of(a.hour24, 0))
                        ProAppointmentItem(
                            id = a.id,
                            patientName = names[a.patientId] ?: "Paciente",
                            dateTime = dt,
                            confirmed = a.confirmed,
                            notes = a.notes
                        )
                    }
                    .sortedWith(
                        compareBy<ProAppointmentItem> { it.dateTime.toLocalDate().toEpochDay() }
                            .thenBy { it.dateTime.hour }
                            .thenBy { it.dateTime.minute }
                    )

                val past = pastList
                    .map { a ->
                        val date = LocalDate.ofEpochDay(a.dateEpochDay)
                        val dt   = LocalDateTime.of(date, LocalTime.of(a.hour24, 0))
                        ProAppointmentItem(
                            id = a.id,
                            patientName = names[a.patientId] ?: "Paciente",
                            dateTime = dt,
                            confirmed = a.confirmed,
                            notes = a.notes
                        )
                    }
                    .sortedWith(
                        compareByDescending<ProAppointmentItem> { it.dateTime.toLocalDate().toEpochDay() }
                            .thenByDescending { it.dateTime.hour }
                            .thenByDescending { it.dateTime.minute }
                    )

                _ui.value = ProfessionalAppointmentsUiState.Content(upcoming, past)
            }
            is Result.Error -> _ui.value = ProfessionalAppointmentsUiState.Error(res.message)
            else -> Unit
        }
    }

    private suspend fun fetchPatientNames(ids: Set<String>): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()
        return try {
            val col = FirebaseFirestore.getInstance().collection("patients")
            val chunks = ids.chunked(10)
            val jobs = chunks.map { batch ->
                viewModelScope.async {
                    val snap = col.whereIn(FieldPath.documentId(), batch).get().await()
                    snap.documents.associate { d -> d.id to (d.getString("fullName") ?: "Paciente") }
                }
            }
            jobs.awaitAll().fold(emptyMap()) { acc, m -> acc + m }
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
