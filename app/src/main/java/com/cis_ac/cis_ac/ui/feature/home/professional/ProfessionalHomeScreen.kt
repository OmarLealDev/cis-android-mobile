package com.cis_ac.cis_ac.ui.feature.home.professional

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cis_ac.cis_ac.ui.feature.shared.BottomBarItem
import com.cis_ac.cis_ac.ui.feature.shared.QuickActionCard
import com.cis_ac.cis_ac.ui.feature.shared.UserBottomBar
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfessionalHomeRoute(
    vm: ProfessionalHomeViewModel = viewModel(),
    onNavigate: (ProfessionalHomeNav) -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = androidx.lifecycle.LifecycleEventObserver { _, e ->
            if (e == androidx.lifecycle.Lifecycle.Event.ON_RESUME) vm.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    val tabs = listOf(
        BottomBarItem("home",        "Inicio",       Icons.Filled.Home),
        BottomBarItem("patients",    "Pacientes",    Icons.Filled.Groups),
        BottomBarItem("appointments","Citas",        Icons.Filled.CalendarMonth),
        BottomBarItem("messages",    "Mensajes",     Icons.Filled.ChatBubbleOutline),
        BottomBarItem("networks",    "Redes",        Icons.Filled.MenuBook),
    )

    Scaffold(
        topBar = { ProfessionalTopBar(ui, onOpenProfile = { onNavigate(ProfessionalHomeNav.Profile) }) },
        bottomBar = {
            UserBottomBar(
                items = tabs,
                selectedId = "home",
                onSelect = { item ->
                    when (item.id) {
                        "patients"     -> onNavigate(ProfessionalHomeNav.Patients)
                        "appointments" -> onNavigate(ProfessionalHomeNav.Appointments)
                        "messages"     -> onNavigate(ProfessionalHomeNav.Messages)
                        "networks"     -> onNavigate(ProfessionalHomeNav.Networks)
                        else -> Unit
                    }
                }
            )
        }
    ) { padding ->
        when (val s = ui) {
            ProfessionalHomeUiState.Loading ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            is ProfessionalHomeUiState.Error ->
                Column(
                    Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    ElevatedButton(onClick = { vm.refresh() }) { Text("Reintentar") }
                }

            is ProfessionalHomeUiState.Content ->
                ProfessionalHomeScreen(
                    profile = s.profile,
                    nextAppointments = s.nextAppointments,
                    onOpenAppointment = { id -> onNavigate(ProfessionalHomeNav.AppointmentDetail(id)) },
                    onOpenAppointmentsTab = { onNavigate(ProfessionalHomeNav.Appointments) },
                    onPatients = { onNavigate(ProfessionalHomeNav.Patients) },
                    onMessages = { onNavigate(ProfessionalHomeNav.Messages) },
                    onAvailability = { onNavigate(ProfessionalHomeNav.Availability) },
                    onNetworks = { onNavigate(ProfessionalHomeNav.Networks) },
                    contentPadding = padding
                )
        }
    }
}

@Composable
private fun ProfessionalTopBar(
    ui: ProfessionalHomeUiState,
    onOpenProfile: () -> Unit
) {
    Surface(tonalElevation = 0.dp, shadowElevation = 2.dp) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(36.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onOpenProfile),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Person, null) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Bienvenido de nuevo,",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val name = (ui as? ProfessionalHomeUiState.Content)?.profile?.displayName ?: "Profesional"
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun ProfessionalHomeScreen(
    profile: ProfessionalProfile,
    nextAppointments: List<ProNextAppointmentItem>,
    onOpenAppointment: (String) -> Unit,
    onOpenAppointmentsTab: () -> Unit,
    onPatients: () -> Unit,
    onMessages: () -> Unit,
    onAvailability: () -> Unit,
    onNetworks: () -> Unit,
    contentPadding: PaddingValues
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        Text("Próximas citas", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
        Spacer(Modifier.height(10.dp))

        if (nextAppointments.isEmpty()) {
            NoAppointmentCard(onClick = onOpenAppointmentsTab)
        } else {
            nextAppointments.take(2).forEach { appt ->
                NextAppointmentCard(appt, onClick = { onOpenAppointment(appt.id) })
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(22.dp))
        Text("Accesos rápidos", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionCard("Pacientes", Icons.Filled.Groups, onClick = onPatients, modifier = Modifier.weight(1f))
            QuickActionCard("Mensajes", Icons.Filled.ChatBubbleOutline, onClick = onMessages, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionCard("Disponibilidad", Icons.Filled.AccessTime, onClick = onAvailability, modifier = Modifier.weight(1f))
            QuickActionCard("Citas", Icons.Filled.CalendarMonth, onClick = onOpenAppointmentsTab, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
    }
}
@Composable
private fun NextAppointmentCard(appt: ProNextAppointmentItem, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
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
                    "Consulta con ${appt.patientName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(if (appt.firstTime) "Primera consulta" else "Seguimiento") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (appt.firstTime)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = if (appt.firstTime)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )

                Spacer(Modifier.height(6.dp))
                Text(
                    formatDayTime(appt.dateTimeMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(Icons.Filled.ArrowForwardIos, contentDescription = null)
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
                Text("Revisa tu agenda o disponibilidad", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ArrowForwardIos, contentDescription = null)
        }
    }
}


private fun formatDayTimePair(millis: Long): Pair<String, String> {
    val tz = TimeZone.getTimeZone("America/Mazatlan")
    val day = SimpleDateFormat("dd 'de' MMMM", Locale.getDefault()).apply { timeZone = tz }
        .format(Date(millis))
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).apply { timeZone = tz }
        .format(Date(millis))
    return day to time
}

private fun formatDayTime(millis: Long): String {
    val (day, time) = formatDayTimePair(millis)
    return "$day, $time"
}