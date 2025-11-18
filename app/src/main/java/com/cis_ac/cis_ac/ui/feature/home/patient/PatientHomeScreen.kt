    package com.cis_ac.cis_ac.ui.feature.home.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cis_ac.cis_ac.ui.feature.home.patient.components.ExploreProfessionalsCard
import com.cis_ac.cis_ac.ui.feature.shared.BottomBarItem
import com.cis_ac.cis_ac.ui.feature.shared.QuickActionCard
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.clickable

@Composable
fun PatientHomeScreen(
    state: PatientHomeUiState,
    onQuickClinicalHistory: () -> Unit,
    onQuickSupportNetwork: () -> Unit,
    onExploreProfessionals: () -> Unit,
    onOpenAppointmentsTab: () -> Unit,
    onOpenMessagesTab: () -> Unit,
    onRefresh: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    onOpenProfile: () -> Unit,
) {


    Surface(Modifier.fillMaxSize()) {
        when (state) {
            is PatientHomeUiState.Loading -> LoadingPlaceholder()
            is PatientHomeUiState.Error -> ErrorPlaceholder(state.message, onRefresh)
            is PatientHomeUiState.Empty -> EmptyPlaceholder(onRefresh)
            is PatientHomeUiState.Content -> Content(
                profile = state.profile,
                nextAppointment = state.nextAppointment,
                onQuickClinicalHistory = onQuickClinicalHistory,
                onQuickSupportNetwork = onQuickSupportNetwork,
                onExploreProfessionals = onExploreProfessionals,
                onOpenAppointmentsTab = onOpenAppointmentsTab,
                onOpenMessagesTab = onOpenMessagesTab,
                contentPadding = contentPadding,
                onOpenProfile = onOpenProfile
            )
        }
    }
}

@Composable
private fun Content(
    profile: PatientProfile,
    nextAppointment: NextAppointment?,
    onQuickClinicalHistory: () -> Unit,
    onQuickSupportNetwork: () -> Unit,
    onExploreProfessionals: () -> Unit,
    onOpenAppointmentsTab: () -> Unit,
    onOpenMessagesTab: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    onOpenProfile: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        HomeTopBar(
                name = profile.displayName,
                onProfileClick = onOpenProfile
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(contentPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(18.dp))
            Text("Próxima cita", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
            Spacer(Modifier.height(10.dp))
            if (nextAppointment != null) NextAppointmentCard(nextAppointment)
            else NoAppointmentCard(onClick = onOpenAppointmentsTab)

            Spacer(Modifier.height(22.dp))
            Text("Accesos rápidos", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickActionCard(
                    title = "Historial clínico",
                    icon = Icons.Default.History,
                    onClick = onQuickClinicalHistory,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "Red de apoyo",
                    icon = Icons.Default.Groups,
                    onClick = onQuickSupportNetwork,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(22.dp))
            Text("Explorar", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
            Spacer(Modifier.height(10.dp))
            ExploreProfessionalsCard(onClick = onExploreProfessionals)

            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.weight(1f))
    }
}
@Composable
private fun HomeTopBar(
    name: String,
    onProfileClick: () -> Unit ) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable{ onProfileClick() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Person, contentDescription = null) }

                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Bienvenido de nuevo,",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 1.dp)
        }
    }
}
    @Composable
    private fun NextAppointmentCard(appointment: NextAppointment) {
        ElevatedCard(

            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(2.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Person, contentDescription = null) }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        appointment.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        appointment.professionalName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val formatted = remember(appointment.dateTime) {
                        val today = LocalDateTime.now().toLocalDate()
                        val day = when (appointment.dateTime.toLocalDate()) {
                            today -> "Hoy"
                            today.plusDays(1) -> "Mañana"
                            else -> appointment.dateTime.format(DateTimeFormatter.ofPattern("dd MMM"))
                        }
                        "$day, " + appointment.dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                    }
                    Text(
                        formatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }
        }
    }
@Composable
private fun NoAppointmentCard(onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, shape = RoundedCornerShape(16.dp), elevation = CardDefaults.elevatedCardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("No tienes citas próximas", style = MaterialTheme.typography.titleMedium)
                Text("Agenda una nueva cita", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ArrowForwardIos, contentDescription = null)
        }
    }
}

@Composable
private fun LoadingPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Cargando…") }
}
@Composable
private fun ErrorPlaceholder(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Ocurrió un error: $message", color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        ElevatedButton(onClick = onRetry) { Text("Reintentar") }
    }
}
@Composable
private fun EmptyPlaceholder(onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Sin datos por ahora")
        Spacer(Modifier.height(8.dp))
        ElevatedButton(onClick = onRetry) { Text("Actualizar") }
    }
}
