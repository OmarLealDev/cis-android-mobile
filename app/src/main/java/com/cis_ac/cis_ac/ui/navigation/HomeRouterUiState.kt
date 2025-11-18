package com.cis_ac.cis_ac.ui.navigation

import androidx.compose.runtime.Immutable
import com.cis_ac.cis_ac.core.model.UserRole

@Immutable
sealed interface HomeRouterUiState {
    object Loading : HomeRouterUiState
    data class Ready(val role: UserRole) : HomeRouterUiState
    object NoSession : HomeRouterUiState
    data class Error(val message: String) : HomeRouterUiState
}