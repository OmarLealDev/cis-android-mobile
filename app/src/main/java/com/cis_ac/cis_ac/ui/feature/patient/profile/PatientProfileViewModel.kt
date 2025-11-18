package com.cis_ac.cis_ac.ui.feature.patient.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.data.auth.AuthRepository
import com.cis_ac.cis_ac.data.auth.FirebaseAuthRepository
import com.cis_ac.cis_ac.data.patient.FirestorePatientProfileRepository
import com.cis_ac.cis_ac.data.patient.PatientProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PatientProfileViewModel(
    private val repo: PatientProfileRepository = FirestorePatientProfileRepository(),
    private val authRepo: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow<PatientProfileUiState>(PatientProfileUiState.Loading)
    val ui: StateFlow<PatientProfileUiState> = _ui

    private val _signedOut = MutableStateFlow(false)
    val signedOut: StateFlow<Boolean> = _signedOut

    fun load() {
        viewModelScope.launch {
            _ui.value = PatientProfileUiState.Loading
            when (val res = repo.getCurrent()) {
                is Result.Success -> {
                    val p = res.data
                    _ui.value = PatientProfileUiState.Content(
                        uid = p.uid,
                        fullName = p.fullName,
                        email = p.email,
                        phone = p.phone
                    )
                }
                is Result.Error -> _ui.value = PatientProfileUiState.Error(res.message)
                else -> Unit
            }
        }
    }

    fun toggleEditing() = _ui.update { s ->
        (s as? PatientProfileUiState.Content)?.copy(isEditing = !s.isEditing) ?: s
    }

    fun onNameChange(v: String) = _ui.update { s ->
        (s as? PatientProfileUiState.Content)?.copy(fullName = v) ?: s
    }

    fun onEmailChange(v: String) = _ui.update { s ->
        (s as? PatientProfileUiState.Content)?.copy(email = v) ?: s
    }

    fun onPhoneChange(v: String) = _ui.update { s ->
        (s as? PatientProfileUiState.Content)?.copy(phone = v) ?: s
    }

    fun cancelEdit() = _ui.update { s ->
        load()
        s
    }

    fun save() {
        val s = _ui.value as? PatientProfileUiState.Content ?: return
        viewModelScope.launch {
            _ui.value = s.copy(isSaving = true)
            when (val res = repo.update(s.fullName, s.email, s.phone)) {
                is Result.Success -> {
                    _ui.value = s.copy(isEditing = false, isSaving = false, saveOk = true)
                }
                is Result.Error -> {
                    _ui.value = PatientProfileUiState.Error(res.message)
                }
                else -> Unit
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            when (authRepo.signOut()) {
                is Result.Success -> _signedOut.value = true
                is Result.Error   -> {  }
                else -> Unit
            }
        }
    }
}
