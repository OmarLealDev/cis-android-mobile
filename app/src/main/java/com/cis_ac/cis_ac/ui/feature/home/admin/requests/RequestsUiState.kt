package com.cis_ac.cis_ac.ui.feature.home.admin.requests

import androidx.compose.runtime.Immutable
import com.cis_ac.cis_ac.core.model.Professional

@Immutable
data class RequestsUiState(
    val loading: Boolean = true,
    val error: String = "",
    val pending: List<Professional> = emptyList()
)
