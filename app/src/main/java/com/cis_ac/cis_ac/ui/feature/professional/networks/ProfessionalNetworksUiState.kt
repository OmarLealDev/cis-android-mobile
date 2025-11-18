package com.cis_ac.cis_ac.ui.feature.professional.networks

import androidx.compose.runtime.Immutable
import com.cis_ac.cis_ac.core.model.social.Post

@Immutable
data class ProfessionalNetworksUiState(
    val loading: Boolean = false,
    val error: String = "",
    val posts: List<Post> = emptyList()
)
