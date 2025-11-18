package com.cis_ac.cis_ac.ui.feature.home.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.data.admin.AdminHomeRepository
import com.cis_ac.cis_ac.data.admin.FirestoreAdminHomeRepository
import com.cis_ac.cis_ac.data.auth.AuthRepository
import com.cis_ac.cis_ac.data.auth.FirebaseAuthRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminHomeViewModel(
    private val adminRepo: AdminHomeRepository = FirestoreAdminHomeRepository(),
    private val authRepo: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminHomeUiState>(AdminHomeUiState.Loading)
    val uiState: StateFlow<AdminHomeUiState> = _uiState

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = AdminHomeUiState.Loading
            try {
                val activeDef = async { adminRepo.getActiveProfessionalsCount() }
                val pendingDef = async { adminRepo.getPendingRequestsCount() }
                val nameDef   = async { adminRepo.getAdminDisplayName() }

                val active = activeDef.await()
                val pending = pendingDef.await()
                val name = nameDef.await()

                _uiState.value = AdminHomeUiState.Content(
                    activeCount = active,
                    pendingCount = pending,
                    adminName = name.ifBlank { "Administrador" }
                )
            } catch (t: Throwable) {
                _uiState.value = AdminHomeUiState.Error(t.message ?: "Error al cargar datos")
            }
        }
    }

    fun signOut(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val res = authRepo.signOut()) {
                is Result.Success -> onSuccess()
                is Result.Error   -> onError(res.message)
                else              -> onError("No se pudo cerrar sesión")
            }
        }
    }
}
