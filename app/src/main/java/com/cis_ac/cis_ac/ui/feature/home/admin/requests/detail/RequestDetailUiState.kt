package com.cis_ac.cis_ac.ui.feature.home.admin.requests.detail

import androidx.compose.runtime.Immutable
import com.cis_ac.cis_ac.core.model.Professional

@Immutable
sealed interface RequestDetailUiState {
    object Loading : RequestDetailUiState
    data class Content(val pro: Professional) : RequestDetailUiState
    data class Error(val message: String) : RequestDetailUiState
    object Done : RequestDetailUiState
}