package com.cis_ac.cis_ac.ui.feature.home.admin.professionals

import androidx.compose.runtime.Immutable
import com.cis_ac.cis_ac.core.model.Professional

enum class ProFilter { ALL, ACTIVE, INACTIVE }

@Immutable
data class ProfessionalsUiState(
    val loading: Boolean = true,
    val error: String = "",
    val all: List<Professional> = emptyList(),
    val query: String = "",
    val filter: ProFilter = ProFilter.ALL
) {
    val visible: List<Professional>
        get() {
            val base = when (filter) {
                ProFilter.ALL -> all
                ProFilter.ACTIVE -> all.filter { it.active }
                ProFilter.INACTIVE -> all.filter { !it.active }
            }
            val q = query.trim().lowercase()
            return if (q.isEmpty()) base
            else base.filter { it.fullName.lowercase().contains(q) }
        }
}
