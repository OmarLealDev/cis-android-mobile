package com.cis_ac.cis_ac.ui.feature.patient.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.UserRole
import com.cis_ac.cis_ac.core.model.history.GeneralInfo
import com.cis_ac.cis_ac.core.model.history.HistoryEntry
import com.cis_ac.cis_ac.core.model.history.UserRef
import com.cis_ac.cis_ac.data.patient.history.FirestorePatientHistoryRepository
import com.cis_ac.cis_ac.data.patient.history.PatientHistoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private fun today(): String =
    SimpleDateFormat("dd/MM/yyyy", Locale("es","MX")).format(Date())

class PatientHistoryViewModel(
    private val repo: PatientHistoryRepository = FirestorePatientHistoryRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val targetPatientId: String? = null
) : ViewModel() {

    private val _ui = MutableStateFlow(PatientHistoryUiState(loading = true))
    val uiState: StateFlow<PatientHistoryUiState> = _ui

    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val message = _message.asSharedFlow()



    private val patientId: String
        get() = targetPatientId ?: auth.currentUser?.uid.orEmpty()

    private var cachedActor: UserRef? = null
    private suspend fun resolveCurrentActor(): UserRef {
        cachedActor?.let { return it }
        val uid = auth.currentUser?.uid.orEmpty()
        val emailFallback = auth.currentUser?.email ?: "Usuario"

        return try {
            val p = db.collection("patients").document(uid).get().await()
            if (p.exists()) {
                val name = p.getString("fullName") ?: emailFallback
                UserRef(uid = uid, name = name, role = UserRole.PATIENT).also { cachedActor = it }
            } else {
                val pro = db.collection("professionals").document(uid).get().await()
                if (pro.exists()) {
                    val name = pro.getString("fullName") ?: emailFallback
                    UserRef(uid = uid, name = name, role = UserRole.PROFESSIONAL).also { cachedActor = it }
                } else {
                    UserRef(uid = uid, name = emailFallback, role = UserRole.PATIENT).also { cachedActor = it }
                }
            }
        } catch (_: Exception) {
            UserRef(uid = uid, name = emailFallback, role = UserRole.PATIENT).also { cachedActor = it }
        }
    }

    init { load() }

    fun load() {
        viewModelScope.launch {
            if (patientId.isBlank()) {
                _ui.value = _ui.value.copy(loading = false, error = "No hay sesión activa")
                return@launch
            }
            _ui.value = _ui.value.copy(loading = true, error = "")
            try {
                val g = async { repo.loadGeneral(patientId) }
                val d = async { repo.listNotes(patientId, "diagnoses") }
                val t = async { repo.listNotes(patientId, "treatments") }
                val m = async { repo.listNotes(patientId, "medications") }
                val a = async { repo.listNotes(patientId, "allergies") }

                val (gRes, dRes, tRes, mRes, aRes) = awaitAll(g, d, t, m, a)

                val general     = (gRes as? Result.Success<*>)?.data as? GeneralInfo ?: GeneralInfo()
                val diagnoses   = (dRes as? Result.Success<*>)?.data as? List<HistoryEntry> ?: emptyList()
                val treatments  = (tRes as? Result.Success<*>)?.data as? List<HistoryEntry> ?: emptyList()
                val medications = (mRes as? Result.Success<*>)?.data as? List<HistoryEntry> ?: emptyList()
                val allergies   = (aRes as? Result.Success<*>)?.data as? List<HistoryEntry> ?: emptyList()

                _ui.value = PatientHistoryUiState(
                    loading = false,
                    generalExpanded = false,
                    diagnoses = diagnoses,
                    treatments = treatments,
                    medications = medications,
                    allergies = allergies,
                    general = general
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(loading = false, error = e.localizedMessage ?: "Error al cargar")
            }
        }
    }

    fun toggleGeneral()    { _ui.value = _ui.value.copy(generalExpanded    = !_ui.value.generalExpanded) }
    fun toggleDiagnoses()  { _ui.value = _ui.value.copy(diagnosesExpanded  = !_ui.value.diagnosesExpanded) }
    fun toggleTreatments() { _ui.value = _ui.value.copy(treatmentsExpanded = !_ui.value.treatmentsExpanded) }
    fun toggleMeds()       { _ui.value = _ui.value.copy(medsExpanded       = !_ui.value.medsExpanded) }
    fun toggleAllergies()  { _ui.value = _ui.value.copy(allergiesExpanded  = !_ui.value.allergiesExpanded) }

    fun onGeneralChange(newInfo: GeneralInfo) {
        _ui.value = _ui.value.copy(general = newInfo, canSave = true)
    }

    fun save() {
        viewModelScope.launch {
            val actor = resolveCurrentActor()
            when (repo.saveGeneral(patientId, _ui.value.general, actor)) {
                is Result.Success -> {
                    _ui.value = _ui.value.copy(canSave = false)
                    _message.tryEmit("Datos guardados correctamente")
                }
                is Result.Error -> _message.tryEmit("No se pudo guardar")
                else -> Unit
            }
        }
    }

    fun addNoteToDiagnoses()  = addDraft("diagnoses")
    fun addNoteToTreatments() = addDraft("treatments")
    fun addNoteToMeds()       = addDraft("medications")
    fun addNoteToAllergies()  = addDraft("allergies")

    private fun addDraft(section: String) {
        viewModelScope.launch {
            val actor = resolveCurrentActor()
            val draft = HistoryEntry(
                id = UUID.randomUUID().toString(),
                section = section,
                date = today(),
                text = "",
                createdBy = actor,
                createdAt = null
            )
            _ui.value = when (section) {
                "diagnoses"   -> _ui.value.copy(diagnoses   = _ui.value.diagnoses + draft,   editingNote = draft)
                "treatments"  -> _ui.value.copy(treatments = _ui.value.treatments + draft,  editingNote = draft)
                "medications" -> _ui.value.copy(medications= _ui.value.medications + draft, editingNote = draft)
                else          -> _ui.value.copy(allergies  = _ui.value.allergies + draft,   editingNote = draft)
            }
        }
    }

    fun onRequestEditNote(entry: HistoryEntry) {
        _ui.value = _ui.value.copy(editingNote = entry)
    }

    fun onCommitNote(entry: HistoryEntry, newDate: String, newText: String) {
        viewModelScope.launch {
            val actor = resolveCurrentActor()

            if (entry.createdAt == null) {
                when (repo.addNote(patientId, entry.section, newDate.ifBlank { today() }, newText, actor)) {
                    is Result.Success -> {
                        _message.tryEmit("Nota agregada correctamente")
                        reloadSection(entry.section)
                    }
                    is Result.Error -> _message.tryEmit("No se pudo agregar la nota")
                    else -> Unit
                }
            } else {
                when (repo.updateNote(patientId, entry.id, newDate.ifBlank { today() }, newText, actor)) {
                    is Result.Success -> {
                        _message.tryEmit("Nota actualizada correctamente")
                        reloadSection(entry.section)
                    }
                    is Result.Error -> _message.tryEmit("No se pudo actualizar la nota")
                    else -> Unit
                }
            }
            _ui.value = _ui.value.copy(editingNote = null)
        }
    }

    private fun reloadSection(section: String) = viewModelScope.launch {
        when (val res = repo.listNotes(patientId, section)) {
            is Result.Success -> {
                _ui.value = when (section) {
                    "diagnoses"   -> _ui.value.copy(diagnoses = res.data)
                    "treatments"  -> _ui.value.copy(treatments = res.data)
                    "medications" -> _ui.value.copy(medications = res.data)
                    else          -> _ui.value.copy(allergies = res.data)
                }
            }
            is Result.Error -> { }
            else -> Unit
        }
    }

    fun dismissEditor() {
        _ui.value = _ui.value.copy(editingNote = null)
    }
}

