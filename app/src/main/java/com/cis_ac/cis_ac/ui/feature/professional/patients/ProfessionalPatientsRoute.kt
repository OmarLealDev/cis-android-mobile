@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.cis_ac.cis_ac.ui.feature.professional.patients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProfessionalPatientsRoute(
    vm: ProfessionalPatientsViewModel = viewModel(),
    onBack: () -> Unit,
    onOpenHistory: (String) -> Unit,
    onMessage: (String) -> Unit
) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 2.dp) {
                Column {
                    CenterAlignedTopAppBar(
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                        title = { Text("Pacientes") }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
        }
    ) { padding ->
        when (val s = state) {
            ProfessionalPatientsUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is ProfessionalPatientsUiState.Error -> Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                ElevatedButton(onClick = vm::load) { Text("Reintentar") }
            }

            is ProfessionalPatientsUiState.Content -> {
                var orderMenu by remember { mutableStateOf(false) }

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SearchField(
                        value = s.query,
                        onValueChange = vm::onQueryChange,
                        placeholder = "Buscar paciente"
                    )

                    Box {
                        OutlinedButton(onClick = { orderMenu = true }) {
                            Text(
                                when (s.order) {
                                    ProfessionalPatientsUiState.Order.LAST_VISIT_DESC -> "Ordenar: Última consulta"
                                    ProfessionalPatientsUiState.Order.NAME_ASC -> "Ordenar: Nombre (A-Z)"
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = orderMenu,
                            onDismissRequest = { orderMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Última consulta") },
                                onClick = {
                                    vm.onChangeOrder(ProfessionalPatientsUiState.Order.LAST_VISIT_DESC)
                                    orderMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Nombre (A-Z)") },
                                onClick = {
                                    vm.onChangeOrder(ProfessionalPatientsUiState.Order.NAME_ASC)
                                    orderMenu = false
                                }
                            )
                        }
                    }

                    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                    s.visible.forEach { item ->
                        ProPatientCard(
                            name = item.fullName,
                            lastVisit = item.lastVisit?.format(fmt) ?: "—",
                            onHistory = { onOpenHistory(item.uid) },
                            onMessage = { onMessage(item.uid) }
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

/* ----------------- UI components ----------------- */

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedCard {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Search, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (value.isBlank()) {
                        Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    inner()
                }
            )
        }
    }
}

@Composable
private fun ProPatientCard(
    name: String,
    lastVisit: String,
    onHistory: () -> Unit,
    onMessage: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Última consulta: $lastVisit", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Filled.Person, contentDescription = null)
            }

            Text(name, style = MaterialTheme.typography.titleMedium)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onHistory, modifier = Modifier.weight(1f)) { Text("Ver historial") }
                FilledTonalButton(onClick = onMessage, modifier = Modifier.weight(1f)) { Text("Enviar mensaje") }
            }
        }
    }
}
