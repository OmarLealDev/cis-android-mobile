package com.cis_ac.cis_ac.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.data.userprofile.FirestoreUserProfileRepository
import com.cis_ac.cis_ac.data.userprofile.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.data.auth.AuthRepository
import com.cis_ac.cis_ac.data.auth.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth


class HomeRouterViewModel(
    private val repo: UserProfileRepository = FirestoreUserProfileRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeRouterUiState>(HomeRouterUiState.Loading)
    val uiState: StateFlow<HomeRouterUiState> = _uiState

    init { resolve() }

    fun resolve() {
        viewModelScope.launch {
            _uiState.value = HomeRouterUiState.Loading
            val uid = auth.currentUser?.uid
            if (uid == null) { _uiState.value = HomeRouterUiState.NoSession; return@launch }

            when (val res = repo.getCurrentUserProfile(uid)) {
                is Result.Success -> _uiState.value = HomeRouterUiState.Ready(res.data.role)
                is Result.Error   -> _uiState.value = HomeRouterUiState.Error(res.message)
                else              -> _uiState.value = HomeRouterUiState.Error("Unknown error")
            }
        }
    }
}
