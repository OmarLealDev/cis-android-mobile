package com.cis_ac.cis_ac.ui.feature.professional.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.data.appointments.AppointmentRepository
import com.cis_ac.cis_ac.data.appointments.FirestoreAppointmentRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ProfessionalPatientsViewModel(
    private val repo: AppointmentRepository = FirestoreAppointmentRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _ui = MutableStateFlow<ProfessionalPatientsUiState>(ProfessionalPatientsUiState.Loading)
    val ui: StateFlow<ProfessionalPatientsUiState> = _ui

    fun load() = viewModelScope.launch {
        _ui.value = ProfessionalPatientsUiState.Loading

        val proId = auth.currentUser?.uid ?: run {
            _ui.value = ProfessionalPatientsUiState.Error("No hay sesión activa")
            return@launch
        }

        when (val res = repo.listProfessionalAppointments(proId)) {
            is Result.Success -> {
                val appts = res.data
                    .filter { it.active }
                    .filter { !it.patientId.isNullOrBlank() }

                val lastByPatient: Map<String, LocalDateTime> =
                    appts.groupBy { it.patientId!! }.mapValues { (_, list) ->
                        val last = list.maxBy { a ->
                            (a.dateEpochDay shl 8) + a.hour24
                        }
                        LocalDateTime.of(LocalDate.ofEpochDay(last.dateEpochDay), LocalTime.of(last.hour24, 0))
                    }

                val patientIds = lastByPatient.keys
                val names = fetchPatientNames(patientIds)

                val activeSet = fetchPatientActiveFlags(patientIds)

                val items = lastByPatient.mapNotNull { (pid, dt) ->
                    val active = activeSet[pid] ?: true
                    if (!active) return@mapNotNull null
                    ProPatientItem(
                        uid = pid,
                        fullName = names[pid] ?: "Paciente",
                        lastVisit = dt
                    )
                }

                val ordered = items.sortedByDescending { it.lastVisit }
                _ui.value = ProfessionalPatientsUiState.Content(
                    query = "",
                    order = ProfessionalPatientsUiState.Order.LAST_VISIT_DESC,
                    items = ordered,
                    visible = ordered
                )
            }
            is Result.Error -> _ui.value = ProfessionalPatientsUiState.Error(res.message)
            else -> Unit
        }
    }

    private suspend fun fetchPatientNames(ids: Set<String>): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()
        return try {
            val col = db.collection("patients")
            val tasks = ids.map { id -> col.document(id).get() }
            @Suppress("UNCHECKED_CAST")
            val snaps = Tasks.whenAllSuccess<DocumentSnapshot>(tasks).await() as List<DocumentSnapshot>
            snaps.mapNotNull { s ->
                val name = s.getString("fullName")
                if (name.isNullOrBlank()) null else s.id to name
            }.toMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private suspend fun fetchPatientActiveFlags(ids: Set<String>): Map<String, Boolean> {
        if (ids.isEmpty()) return emptyMap()
        return try {
            val col = db.collection("patients")
            val tasks = ids.map { id -> col.document(id).get() }
            @Suppress("UNCHECKED_CAST")
            val snaps = Tasks.whenAllSuccess<DocumentSnapshot>(tasks).await() as List<DocumentSnapshot>
            snaps.associate { s ->
                val flag = s.getBoolean("active") ?: true
                s.id to flag
            }
        } catch (_: Exception) {
            ids.associateWith { true }
        }
    }

    fun onQueryChange(q: String) {
        val s = _ui.value as? ProfessionalPatientsUiState.Content ?: return
        val filtered = filterAndOrder(s.items, q, s.order)
        _ui.value = s.copy(query = q, visible = filtered)
    }

    fun onChangeOrder(order: ProfessionalPatientsUiState.Order) {
        val s = _ui.value as? ProfessionalPatientsUiState.Content ?: return
        val filtered = filterAndOrder(s.items, s.query, order)
        _ui.value = s.copy(order = order, visible = filtered)
    }

    private fun filterAndOrder(
        base: List<ProPatientItem>,
        q: String,
        order: ProfessionalPatientsUiState.Order
    ): List<ProPatientItem> {
        val f = if (q.isBlank()) base else base.filter {
            it.fullName.contains(q, ignoreCase = true)
        }
        return when (order) {
            ProfessionalPatientsUiState.Order.LAST_VISIT_DESC ->
                f.sortedByDescending { it.lastVisit }
            ProfessionalPatientsUiState.Order.NAME_ASC ->
                f.sortedBy { it.fullName.lowercase() }
        }
    }
}
