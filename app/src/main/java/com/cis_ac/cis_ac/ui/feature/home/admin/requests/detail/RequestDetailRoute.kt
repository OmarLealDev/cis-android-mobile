package com.cis_ac.cis_ac.ui.feature.home.admin.requests.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cis_ac.cis_ac.core.model.label
import com.cis_ac.cis_ac.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.cis_ac.cis_ac.core.model.label


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailRoute(
    uid: String,
    vm: RequestDetailViewModel = viewModel(),
    onBack: () -> Unit,
    onApproved: () -> Unit = {},
    onRejected: () -> Unit = {},
    showActions: Boolean = true
) {
    val state by vm.ui.collectAsStateWithLifecycle()

    var approving by remember { mutableStateOf(false) }
    var rejecting by remember { mutableStateOf(false) }


    LaunchedEffect(uid) { vm.load(uid) }

    LaunchedEffect(state) {
        when (state) {
            is RequestDetailUiState.Done -> {
                approving = false
                rejecting = false
                onBack()
            }
            is RequestDetailUiState.Error -> {
                approving = false
                rejecting = false
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 2.dp) {
                Column {
                    CenterAlignedTopAppBar(
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                        title = { Text(if (showActions) "Detalles de solicitud" else "Perfil profesional") }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
        }
    ) { padding ->
        when (val s = state) {
            is RequestDetailUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is RequestDetailUiState.Error -> Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                ElevatedButton(onClick = { vm.load(uid) }) { Text("Reintentar") }
            }

            is RequestDetailUiState.Content -> {
                val pro = s.pro
                val ctx = LocalContext.current

                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(Modifier.height(4.dp)) }

                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.large),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.large
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Person, null)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    pro.fullName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(pro.mainDiscipline.label(), style = MaterialTheme.typography.bodyMedium)
                                pro.createdAt?.let {
                                    Text(
                                        "Solicitado: ${formatDate(it)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        SectionTitle("Información personal")
                        InfoCard {
                            InfoRow("Correo electrónico", pro.email)
                            InfoRow("Teléfono", pro.phone.takeIf { it.isNotBlank() } ?: "—")
                            InfoRow("Fecha de nacimiento", pro.dob.takeIf { it.isNotBlank() } ?: "—")
                            InfoRow("Género", pro.gender.label())
                            InfoRow("Número de licencia", pro.licenseNumber.takeIf { it.isNotBlank() } ?: "—")
                            InfoRow("Estado", if (pro.verified) "Verificado" else "Pendiente de verificación")
                        }
                    }

                    item {
                        SectionTitle("Información profesional")
                        InfoCard {
                            InfoRow("Disciplina principal", pro.mainDiscipline.label())
                            InfoRow("Especialidad", pro.speciality.takeIf { it.isNotBlank() } ?: "—")
                            InfoRow("Experiencia", pro.expertiz.takeIf { it.isNotBlank() } ?: "—")
                        }
                    }

                    if (pro.mainDiscipline == com.cis_ac.cis_ac.core.model.Discipline.PSYCHOLOGY) {
                        item {
                            SectionTitle("Enfoque terapéutico")
                            InfoCard {
                                Text(
                                    text = pro.approach?.takeIf { it.isNotBlank() } ?: "—",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    item {
                        SectionTitle("Temas de especialización")
                        InfoCard {
                            Text(
                                text = pro.topics.takeIf { it.isNotBlank() } ?: "—",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    item {
                        SectionTitle("Modalidades de atención")
                        InfoCard {
                            if (pro.modalities.isEmpty()) {
                                Text("—", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                Text(
                                    pro.modalities.joinToString(", ") { it.spanishName },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    item {
                        SectionTitle("Tipos de sesión")
                        InfoCard {
                            if (pro.sessionTypes.isEmpty()) {
                                Text("—", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                Text(
                                    pro.sessionTypes.joinToString(", ") { it.spanishName },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    item {
                        SectionTitle("Poblaciones que atiende")
                        InfoCard {
                            if (pro.populations.isEmpty()) {
                                Text("—", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                Text(
                                    pro.populations.joinToString(", ") { it.spanishName },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    item {
                        SectionTitle("Horarios disponibles")
                        InfoCard {
                            if (pro.schedule.isEmpty()) {
                                Text("—", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    pro.schedule.toSortedMap().forEach { (day, hours) ->
                                        val dayName = when (day) {
                                            1 -> "Lunes"
                                            2 -> "Martes"
                                            3 -> "Miércoles"
                                            4 -> "Jueves"
                                            5 -> "Viernes"
                                            6 -> "Sábado"
                                            7 -> "Domingo"
                                            else -> "Día $day"
                                        }

                                        Surface(
                                            shape = MaterialTheme.shapes.small,
                                            color = MaterialTheme.colorScheme.surface,
                                            tonalElevation = 1.dp,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = dayName,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = MaterialTheme.colorScheme.primary
                                                )

                                                if (hours.isEmpty()) {
                                                    Text(
                                                        text = "Sin horarios disponibles",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                } else {
                                                    val groupedHours = groupConsecutiveHours(hours.sorted())
                                                    Text(
                                                        text = groupedHours.joinToString(", "),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        SectionTitle("Archivos adjuntos")
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FileRow(
                                title = "Curriculum vitae.pdf",
                                enabled = pro.cvUrl != null,
                                onClick = {
                                    pro.cvUrl?.let { url ->
                                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    }
                                }
                            )
                            FileRow(
                                title = "Cédula profesional.pdf",
                                enabled = pro.licenseUrl != null,
                                onClick = {
                                    pro.licenseUrl?.let { url ->
                                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    }
                                }
                            )
                        }
                    }

                    if (showActions) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = {
                                        approving = true
                                        rejecting = false
                                        vm.approve(pro.uid)
                                        onApproved()
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !approving && !rejecting,
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) {
                                    if (approving) {
                                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    Text("Aprobar")
                                }

                                Button(
                                    onClick = {
                                        rejecting = true
                                        approving = false
                                        vm.reject(pro.uid)
                                        onRejected()
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !approving && !rejecting,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    if (rejecting) {
                                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    Text("Rechazar", color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }

            RequestDetailUiState.Done -> Unit
        }
    }
}

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

private fun groupConsecutiveHours(hours: List<Int>): List<String> {
    if (hours.isEmpty()) return emptyList()

    val groups = mutableListOf<String>()
    var start = hours.first()
    var end = start

    for (i in 1 until hours.size) {
        val current = hours[i]
        if (current == end + 1) {
            end = current
        } else {
            groups.add(formatHourRange(start, end))
            start = current
            end = current
        }
    }

    groups.add(formatHourRange(start, end))

    return groups
}

private fun formatHourRange(start: Int, end: Int): String {
    return if (start == end) {
        "${start}:00"
    } else {
        "${start}:00-${end}:00"
    }
}

@Composable
private fun FileRow(
    title: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (enabled) 2.dp else 0.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Description, null)
            Spacer(Modifier.width(10.dp))
            Text(title, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(10.dp))
            Icon(Icons.Filled.Download, null)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun InfoCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

