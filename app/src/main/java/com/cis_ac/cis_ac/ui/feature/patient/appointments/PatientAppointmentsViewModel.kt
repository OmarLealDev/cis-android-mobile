package com.cis_ac.cis_ac.ui.feature.patient.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
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
import java.util.TimeZone
import java.util.Calendar
import com.cis_ac.cis_ac.core.model.label

class PatientAppointmentsViewModel(
    private val repo: AppointmentRepository = FirestoreAppointmentRepository(),
    private val currentPatientIdProvider: () -> String = {
        FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("No hay sesión de paciente")
    }
) : ViewModel() {

    private val _ui = MutableStateFlow<PatientAppointmentsUiState>(PatientAppointmentsUiState.Loading)
    val ui: StateFlow<PatientAppointmentsUiState> = _ui

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _showUpcoming = MutableStateFlow(false)
    private val _showPast     = MutableStateFlow(false)
    val showUpcoming: StateFlow<Boolean> = _showUpcoming
    val showPast: StateFlow<Boolean>     = _showPast

    fun toggleUpcoming() { _showUpcoming.value = !_showUpcoming.value }
    fun togglePast()     { _showPast.value     = !_showPast.value }

    fun load() {
        viewModelScope.launch {
            _ui.value = PatientAppointmentsUiState.Loading

            val patientId = runCatching { currentPatientIdProvider() }.getOrElse {
                _ui.value = PatientAppointmentsUiState.Error("No hay sesión de paciente")
                return@launch
            }

            when (val res = repo.listPatientAppointments(patientId)) {
                is Result.Error -> _ui.value = PatientAppointmentsUiState.Error(res.message)
                is Result.Success -> {
                    val appts = res.data.orEmpty().filter { it.active != false }

                    val proNames = fetchProfessionalNames(appts.mapNotNull { it.professionalId }.toSet())

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
                            AppointmentItem(
                                id = a.id.orEmpty(),
                                professionalName = proNames[a.professionalId] ?: "Profesional",
                                disciplineLabel = a.discipline.label(),
                                dateTime = dt,
                                durationMinutes = 30,
                                confirmed = a.confirmed
                            )
                        }
                        .sortedWith(
                            compareBy<AppointmentItem> { it.dateTime.toLocalDate().toEpochDay() }
                                .thenBy { it.dateTime.hour }
                                .thenBy { it.dateTime.minute }
                        )

                    val past = pastList
                        .map { a ->
                            val date = LocalDate.ofEpochDay(a.dateEpochDay)
                            val dt   = LocalDateTime.of(date, LocalTime.of(a.hour24, 0))
                            AppointmentItem(
                                id = a.id.orEmpty(),
                                professionalName = proNames[a.professionalId] ?: "Profesional",
                                disciplineLabel = a.discipline.label(),
                                dateTime = dt,
                                durationMinutes = 30,
                                confirmed = a.confirmed
                            )
                        }
                        .sortedWith(
                            compareByDescending<AppointmentItem> { it.dateTime.toLocalDate().toEpochDay() }
                                .thenByDescending { it.dateTime.hour }
                                .thenByDescending { it.dateTime.minute }
                        )

                    _ui.value = PatientAppointmentsUiState.Content(upcoming, past)
                }
                else -> Unit
            }
        }
    }

    private suspend fun fetchProfessionalNames(ids: Set<String>): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()
        val chunks = ids.chunked(10)
        val tasks = chunks.map { chunk ->
            viewModelScope.async {
                val snap = db.collection("professionals")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get().await()
                snap.documents.associate { d -> d.id to (d.getString("fullName") ?: "Profesional") }
            }
        }
        return tasks.awaitAll().fold(emptyMap()) { acc, m -> acc + m }
    }

    companion object {
        fun factory(
            repo: AppointmentRepository = FirestoreAppointmentRepository(),
            currentPatientIdProvider: () -> String = {
                FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw IllegalStateException("No hay sesión de paciente")
            }
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PatientAppointmentsViewModel(repo, currentPatientIdProvider) as T
            }
        }
    }
}
