package com.cis_ac.cis_ac.ui.feature.home.admin

import androidx.compose.runtime.Immutable

@Immutable
sealed interface AdminHomeUiState {
    object Loading : AdminHomeUiState
    data class Content(
        val activeCount: Int,
        val pendingCount: Int,
        val adminName: String = "Administrador"
    ) : AdminHomeUiState
    data class Error(val message: String) : AdminHomeUiState
}