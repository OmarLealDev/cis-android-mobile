package com.cis_ac.cis_ac.ui.feature.professional.appointments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cis_ac.cis_ac.ui.feature.professional.appointments.components.ProAppointmentCard

@Composable
fun ProfessionalAppointmentsScreen(
    upcoming: List<ProAppointmentItem>,
    past: List<ProAppointmentItem>,
    showUpcoming: Boolean,
    showPast: Boolean,
    onToggleUpcoming: () -> Unit,
    onTogglePast: () -> Unit,
    onOpen: (String) -> Unit,
    onApprove: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        CollapsibleSectionHeader(
            title = "Próximas citas",
            expanded = showUpcoming,
            onToggle = onToggleUpcoming
        )
        AnimatedVisibility(
            visible = showUpcoming,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (upcoming.isEmpty()) {
                    Text("No tienes próximas citas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    upcoming.forEach { a ->
                        ProAppointmentCard(
                            item = a,
                            onOpen = onOpen,
                            onApprove = onApprove
                        )
                    }
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        CollapsibleSectionHeader(
            title = "Citas pasadas",
            expanded = showPast,
            onToggle = onTogglePast
        )
        AnimatedVisibility(
            visible = showPast,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (past.isEmpty()) {
                    Text("Aún no hay citas pasadas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    past.forEach { a ->
                        ProAppointmentCard(
                            item = a,
                            onOpen = onOpen,
                            onApprove = onApprove
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(72.dp))
    }
}

@Composable
private fun CollapsibleSectionHeader(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        tonalElevation = 1.dp,
        shape = CardDefaults.elevatedShape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }
        }
    }
}
