package com.cis_ac.cis_ac.ui.feature.professional.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.Discipline
import com.cis_ac.cis_ac.core.model.Gender
import com.cis_ac.cis_ac.core.model.Modality
import com.cis_ac.cis_ac.core.model.Population
import com.cis_ac.cis_ac.core.model.Professional
import com.cis_ac.cis_ac.core.model.Sessions
import com.cis_ac.cis_ac.data.professional.FirestoreProfessionalProfileRepository
import com.cis_ac.cis_ac.data.professional.ProfessionalProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import com.cis_ac.cis_ac.data.auth.AuthRepository
import com.cis_ac.cis_ac.data.auth.FirebaseAuthRepository

class ProfessionalProfileViewModel(
    private val repo: ProfessionalProfileRepository = FirestoreProfessionalProfileRepository(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val authRepo: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfessionalProfileUiState>(ProfessionalProfileUiState.Loading)
    val uiState: StateFlow<ProfessionalProfileUiState> = _uiState

    private val _message = MutableSharedFlow<String?>(extraBufferCapacity = 1)
    val message = _message

    private val _signedOut = MutableStateFlow(false)
    val signedOut: StateFlow<Boolean> = _signedOut

    private var original: ProfessionalProfileUiState.Content? = null
    private var currentUid: String? = null


    fun load() = viewModelScope.launch {
        _uiState.value = ProfessionalProfileUiState.Loading
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = ProfessionalProfileUiState.Error("No hay sesión activa")
            return@launch
        }
        currentUid = uid

        when (val res = repo.getProfessional(uid)) {
            is Result.Success -> {
                val p: Professional = res.data

                val snap = db.collection("professionals").document(uid).get().await()
                val cvUrl = snap.getString("cvUrl")
                val licenseUrl = snap.getString("licenseUrl")

                val s = ProfessionalProfileUiState.Content(
                    editing = false,
                    saving = false,
                    verified = p.verified,

                    fullName = p.fullName,
                    email = p.email,
                    phone = p.phone,
                    dob = p.dob,
                    gender = p.gender,

                    discipline = p.mainDiscipline,
                    licenseNumber = p.licenseNumber,
                    speciality = p.speciality,
                    approach = p.approach.orEmpty(),
                    topics = p.topics,
                    expertiz = p.expertiz,

                    modalities = p.modalities,
                    sessionTypes = p.sessionTypes,
                    populations = p.populations,

                    semblance = p.semblance,

                    cvUrl = cvUrl,
                    licenseUrl = licenseUrl
                )
                original = s
                _uiState.value = s
            }
            is Result.Error -> _uiState.value = ProfessionalProfileUiState.Error(res.message)
            else -> Unit
        }
    }


    fun toggleEditing() {
        val s = _uiState.value as? ProfessionalProfileUiState.Content ?: return
        _uiState.value = s.copy(editing = !s.editing)
    }

    fun cancelEdit() {
        original?.let { _uiState.value = it.copy(editing = false, saving = false) }
    }

    fun toast(msg: String) { _message.tryEmit(msg) }
    fun consumeMessage()   { _message.tryEmit(null) }

    private inline fun update(block: (ProfessionalProfileUiState.Content) -> ProfessionalProfileUiState.Content) {
        val s = _uiState.value as? ProfessionalProfileUiState.Content ?: return
        _uiState.value = block(s)
    }


    fun onFullNameChange(v: String) = update { it.copy(fullName = v) }
    fun onPhoneChange(v: String)     = update { it.copy(phone = v) }
    fun onDobChange(v: String)       = update { it.copy(dob = v) }
    fun onGenderChange(v: Gender)    = update { it.copy(gender = v) }

    fun onLicenseChange(v: String)   = update { it.copy(licenseNumber = v) }
    fun onSpecialityChange(v: String)= update { it.copy(speciality = v) }
    fun onApproachChange(v: String)  = update { it.copy(approach = v) }
    fun onTopicsChange(v: String)    = update { it.copy(topics = v) }
    fun onExpertizChange(v: String)  = update { it.copy(expertiz = v) }

    fun toggleModality(m: Modality)  = update {
        val set = it.modalities.toMutableSet()
        if (!set.add(m)) set.remove(m)
        it.copy(modalities = set)
    }
    fun toggleSession(ses: Sessions) = update {
        val set = it.sessionTypes.toMutableSet()
        if (!set.add(ses)) set.remove(ses)
        it.copy(sessionTypes = set)
    }
    fun togglePopulation(p: Population) = update {
        val set = it.populations.toMutableSet()
        if (!set.add(p)) set.remove(p)
        it.copy(populations = set)
    }

    fun onSemblanceChange(v: String) = update { it.copy(semblance = v) }


    fun save() = viewModelScope.launch {
        val s = _uiState.value as? ProfessionalProfileUiState.Content ?: return@launch
        val uid = currentUid ?: return@launch
        _uiState.value = s.copy(saving = true)

        val updates = hashMapOf<String, Any?>(
            "fullName" to s.fullName,
            "phone" to s.phone,
            "dob" to s.dob,
            "gender" to s.gender.name,

            "licenseNumber" to s.licenseNumber,
            "speciality" to s.speciality,
            "approach" to s.approach,
            "topics" to s.topics,
            "expertiz" to s.expertiz,

            "modalities" to s.modalities.toModalityIdList(),
            "sessionTypes" to s.sessionTypes.toSessionsIdList(),
            "populations" to s.populations.toPopulationIdList(),

            "semblance" to s.semblance
        )

        try {
            db.collection("professionals").document(uid).update(updates).await()
            _message.tryEmit("Perfil actualizado")
            val saved = s.copy(editing = false, saving = false)
            original = saved
            _uiState.value = saved
        } catch (e: Exception) {
            _uiState.value = s.copy(saving = false)
            _message.tryEmit(e.message ?: "No se pudo guardar")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            when (authRepo.signOut()) {
                is Result.Success -> _signedOut.value = true
                is Result.Error   -> _message.tryEmit("No se pudo cerrar sesión")
                else -> Unit
            }
        }
    }
}


private fun Modality.id(): Int = when (this) {
    Modality.PRESENCIAL -> 1
    Modality.EN_LINEA   -> 2
    Modality.DOMICILIO  -> 3
}
private fun Sessions.id(): Int = when (this) {
    Sessions.INDIVIDUAL -> 1
    Sessions.PAREJA     -> 2
    Sessions.FAMILIAR   -> 3
    Sessions.EQUIPOS    -> 4
}
private fun Population.id(): Int = when (this) {
    Population.INFANTES        -> 1
    Population.ADOLESCENTES    -> 2
    Population.ADULTOS         -> 3
    Population.ADULTOS_MAYORES -> 4
}

private fun Set<Modality>.toModalityIdList(): List<Int> = this.map { it.id() }
private fun Set<Sessions>.toSessionsIdList(): List<Int> = this.map { it.id() }
private fun Set<Population>.toPopulationIdList(): List<Int> = this.map { it.id() }
