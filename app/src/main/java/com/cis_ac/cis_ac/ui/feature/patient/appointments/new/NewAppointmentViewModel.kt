package com.cis_ac.cis_ac.ui.feature.patient.appointments.new

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.Appointment
import com.cis_ac.cis_ac.core.model.Discipline
import com.cis_ac.cis_ac.data.appointments.AppointmentRepository
import com.cis_ac.cis_ac.data.appointments.FirestoreAppointmentRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDate

class NewAppointmentViewModel(
    private val repo: AppointmentRepository = FirestoreAppointmentRepository(),
    private val currentPatientIdProvider: () -> String? = {
        FirebaseAuth.getInstance().currentUser?.uid
    }
) : ViewModel() {

    private var proSchedule: Map<Int, List<Int>> = emptyMap()

    private val _ui = androidx.compose.runtime.mutableStateOf(NewAppointmentUiState())
    val ui: androidx.compose.runtime.State<NewAppointmentUiState> = _ui

    fun preselect(discipline: Discipline?, professionalId: String?, professionalName: String?) {
        _ui.value = _ui.value.copy(
            selectedDiscipline = discipline,
            selectedProfessional = professionalId?.let { ProfessionalOption(it, professionalName ?: "") }
        )
        if (discipline != null && professionalId == null) loadProfessionals(discipline)
        if (professionalId != null) onProfessionalSelected(professionalId, professionalName ?: "")
    }

    fun onDisciplineSelected(d: Discipline) {
        _ui.value = _ui.value.copy(
            selectedDiscipline = d,
            professionals = emptyList(),
            selectedProfessional = null,
            enabledDays = emptySet(),
            availableHours = emptyList(),
            disabledHours = emptySet(),
            selectedEpochDay = null,
            selectedHour = null
        )
        loadProfessionals(d)
    }

    private fun loadProfessionals(d: Discipline) = viewModelScope.launch {
        _ui.value = _ui.value.copy(isLoading = true, error = "")
        when (val res = repo.listProfessionalsByDiscipline(d)) {
            is Result.Success -> {
                val opts = res.data.map { ProfessionalOption(it.uid, it.fullName) }
                _ui.value = _ui.value.copy(isLoading = false, professionals = opts)
            }
            is Result.Error -> _ui.value = _ui.value.copy(isLoading = false, error = res.message)
            else -> Unit
        }
    }

    fun onProfessionalSelected(uid: String, name: String) = viewModelScope.launch {
        _ui.value = _ui.value.copy(
            selectedProfessional = ProfessionalOption(uid, name),
            selectedEpochDay = null, selectedHour = null,
            availableHours = emptyList(), disabledHours = emptySet(),
            enabledDays = emptySet(), isLoading = true, error = ""
        )

        when (val res = repo.getProfessionalSchedule(uid)) {
            is Result.Success -> {
                proSchedule = normalizeScheduleKeys(res.data)
                val enabled = proSchedule.keys.toSet()
                _ui.value = _ui.value.copy(isLoading = false, enabledDays = enabled)
            }
            is Result.Error -> _ui.value = _ui.value.copy(isLoading = false, error = res.message)
            else -> Unit
        }
    }

    private fun normalizeScheduleKeys(src: Map<Int, List<Int>>): Map<Int, List<Int>> {
        return if (src.keys.any { it == 0 }) {
            src.mapKeys { (k, _) -> if (k == 0) 7 else k }
        } else {
            src
        }
    }

    fun onDateSelected(date: LocalDate) = viewModelScope.launch {
        val dayOfWeek = date.dayOfWeek.value
        val epochDay = date.toEpochDay()

        _ui.value = _ui.value.copy(
            selectedEpochDay = epochDay,
            selectedHour = null,
            isLoading = true,
            error = ""
        )

        var baseHours = proSchedule[dayOfWeek].orEmpty()

        val today = LocalDate.now()
        if (date == today) {
            val currentHour = java.time.ZonedDateTime.now().hour
            baseHours = baseHours.filter { it >= currentHour }
        }

        val proId = _ui.value.selectedProfessional?.uid ?: return@launch
        val booked = when (val res = repo.getBookedHours(proId, epochDay)) {
            is Result.Success -> res.data
            is Result.Error -> {
                _ui.value = _ui.value.copy(isLoading = false, error = res.message)
                return@launch
            }
            else -> emptySet()
        }

        _ui.value = _ui.value.copy(
            isLoading = false,
            availableHours = baseHours,
            disabledHours = booked
        )
    }

    fun onHourSelected(hour: Int) {
        if (hour in _ui.value.disabledHours) return
        _ui.value = _ui.value.copy(selectedHour = hour)
    }

    fun onNotesChange(text: String) {
        _ui.value = _ui.value.copy(notes = text)
    }

    fun canSubmit(): Boolean {
        val s = _ui.value
        return s.selectedDiscipline != null &&
                s.selectedProfessional != null &&
                s.selectedEpochDay != null &&
                s.selectedHour != null
    }

    fun submit(onSuccess: () -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        val s = _ui.value
        if (!canSubmit()) {
            onError("Completa disciplina, profesional, fecha y hora.")
            return@launch
        }
        val appt = Appointment(
            patientId = currentPatientIdProvider(),
            professionalId = s.selectedProfessional!!.uid,
            discipline = s.selectedDiscipline!!,
            dateEpochDay = s.selectedEpochDay!!,
            hour24 = s.selectedHour!!,
            notes = s.notes
        )
        _ui.value = _ui.value.copy(isLoading = true, error = "")
        when (val res = repo.create(appt)) {
            is Result.Success -> {
                _ui.value = _ui.value.copy(isLoading = false, success = true)
                onSuccess()
            }
            is Result.Error -> {
                _ui.value = _ui.value.copy(isLoading = false, error = res.message)
                onError(res.message)
            }
            else -> Unit
        }
    }

    fun consumeSuccess() {
        _ui.value = _ui.value.copy(success = false)
    }

}
