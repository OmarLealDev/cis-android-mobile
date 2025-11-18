package com.cis_ac.cis_ac.ui.feature.professional.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfessionalScheduleViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _ui = MutableStateFlow<ProfessionalScheduleUiState>(ProfessionalScheduleUiState.Loading)
    val ui: StateFlow<ProfessionalScheduleUiState> = _ui

    val message = MutableSharedFlow<String?>(extraBufferCapacity = 1)

    private val buffer: MutableMap<Int, MutableSet<Int>> = mutableMapOf()

    private var original: Map<Int, Set<Int>> = emptyMap()


    fun load() = viewModelScope.launch {
        _ui.value = ProfessionalScheduleUiState.Loading
        val uid = auth.currentUser?.uid ?: run {
            _ui.value = ProfessionalScheduleUiState.Error("No hay sesión activa")
            return@launch
        }
        try {
            val snap = db.collection("professionals").document(uid).get().await()

            val scheduleStr = snap.get("schedule") as? Map<*, *>
            val scheduleInt = snap.get("scheduleInt") as? Map<*, *>

            buffer.clear()

            if (scheduleStr != null) {
                scheduleStr.forEach { (k, v) ->
                    val day = (k as? String)?.toIntOrNull() ?: return@forEach
                    val list = (v as? List<*>)?.mapNotNull {
                        when (it) {
                            is Number -> it.toInt()
                            is String -> it.toIntOrNull()
                            else -> null
                        }
                    } ?: emptyList()
                    buffer[day] = list.filter { it in 0..23 }.toMutableSet()
                }
            } else if (scheduleInt != null) {
                scheduleInt.forEach { (k, v) ->
                    val day = (k as? Number)?.toInt() ?: return@forEach
                    val list = (v as? List<*>)?.mapNotNull {
                        when (it) {
                            is Number -> it.toInt()
                            is String -> it.toIntOrNull()
                            else -> null
                        }
                    } ?: emptyList()
                    buffer[day] = list.filter { it in 0..23 }.toMutableSet()
                }
            }

            original = deepCopy(buffer)
            publish(saving = false)
        } catch (e: Exception) {
            _ui.value = ProfessionalScheduleUiState.Error(e.message ?: "No se pudo cargar la disponibilidad")
        }
    }


    fun addDay(day: Int) {
        if (day in 1..7) {
            buffer.putIfAbsent(day, mutableSetOf())
            publish()
        }
    }

    fun removeDay(day: Int) {
        buffer.remove(day)
        publish()
    }

    fun setDayHours(day: Int, hours: Set<Int>) {
        if (day !in 1..7) return
        buffer[day] = hours.filter { it in 0..23 }.toMutableSet()
        if (buffer[day]?.isEmpty() == true) buffer.remove(day) // no dejamos el día vacío en buffer
        publish()
    }

    fun addHour(day: Int, hour: Int) {
        if (day !in 1..7 || hour !in 0..23) return
        buffer.getOrPut(day) { mutableSetOf() }.add(hour)
        publish()
    }

    fun removeHour(day: Int, hour: Int) {
        buffer[day]?.let {
            it.remove(hour)
            if (it.isEmpty()) buffer.remove(day)
            publish()
        }
    }

    fun discard() {
        buffer.clear()
        original.forEach { (d, set) -> buffer[d] = set.toMutableSet() }
        publish()
        message.tryEmit("Cambios descartados")
    }


    fun save() = viewModelScope.launch {
        val current = _ui.value as? ProfessionalScheduleUiState.Content ?: return@launch

        if (!current.canSave) return@launch

        _ui.value = current.copy(saving = true)

        val uid = auth.currentUser?.uid ?: run {
            _ui.value = current.copy(saving = false)
            message.tryEmit("Error: No hay sesión activa")
            return@launch
        }

        try {
            val sanitized: Map<Int, List<Int>> = buffer
                .filterValues { it.isNotEmpty() }
                .mapValues { (_, set) -> set.toList().distinct().sorted() }

            val scheduleFs: Map<String, List<Int>> = sanitized.mapKeys { (k, _) -> k.toString() }

            db.collection("professionals").document(uid)
                .update(
                    mapOf(
                        "schedule" to scheduleFs,
                        "scheduleInt" to FieldValue.delete()
                    )
                ).await()

            buffer.clear()
            sanitized.forEach { (d, list) -> buffer[d] = list.toMutableSet() }
            original = deepCopy(buffer)

            publish(saving = false)
            message.tryEmit("Agenda actualizada")
        } catch (e: Exception) {
            _ui.value = current.copy(saving = false)
            message.tryEmit("Error: ${e.message ?: "No se pudo guardar"}")
        }
    }


    private fun publish(saving: Boolean = (_ui.value as? ProfessionalScheduleUiState.Content)?.saving ?: false) {
        val items = buffer.entries
            .sortedBy { it.key }
            .map { (day, set) -> DayHours(day = day, hours = set.toList().sorted()) }

        val invalidDays: Set<Int> = buffer
            .filterValues { it.isEmpty() }
            .keys

        val isDirty = !mapsEqual(buffer, original)

        val canSave = isDirty && invalidDays.isEmpty()
        val canDiscard = isDirty

        _ui.value = ProfessionalScheduleUiState.Content(
            days = items,
            saving = saving,
            canSave = canSave,
            canDiscard = canDiscard,
            invalidDays = invalidDays
        )
    }

    private fun deepCopy(src: Map<Int, MutableSet<Int>>): Map<Int, Set<Int>> =
        src.mapValues { (_, v) -> v.toSet() }

    private fun mapsEqual(
        a: Map<Int, MutableSet<Int>>,
        b: Map<Int, Set<Int>>
    ): Boolean {
        if (a.size != b.size) return false
        for ((k, va) in a) {
            val vb = b[k] ?: return false
            if (va.size != vb.size) return false
            if (!va.containsAll(vb)) return false
        }
        return true
    }
}
