@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.cis_ac.cis_ac.ui.feature.professional.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProfessionalScheduleRoute(
    vm: ProfessionalScheduleViewModel = viewModel(),
    onBack: () -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.load() }


    val expandedByDay = rememberSaveable(
        saver = listSaver(
            save = { it.entries.flatMap { (k,v) -> listOf(k, if (v) 1 else 0) } },
            restore = { flat ->
                mutableStateMapOf<Int, Boolean>().apply {
                    flat.chunked(2).forEach { (k, v) -> this[k as Int] = (v as Int) == 1 }
                }
            }
        )
    ) { mutableStateMapOf<Int, Boolean>() }
    var showAddDay by remember { mutableStateOf(false) }
    var editDialogForDay by remember { mutableStateOf<DayHours?>(null) }

    val days = (ui as? ProfessionalScheduleUiState.Content)?.days ?: emptyList()
    val usedDays = days.map { it.day }.toSet()
    val canAddDay = usedDays.size < 7

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        vm.message.collect { msg ->
            msg?.let { snackbar.showSnackbar(it) }
        }
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 2.dp) {
                Column {
                    CenterAlignedTopAppBar(
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                        title = { Text("Disponibilidad") }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            (ui as? ProfessionalScheduleUiState.Content)?.let { s ->
                Surface(shadowElevation = 4.dp) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { vm.discard() },
                            enabled = s.canDiscard && !s.saving,
                            modifier = Modifier.weight(1f)
                        ) { Text("Descartar") }

                        Button(
                            onClick = { vm.save() },
                            enabled = s.canSave && !s.saving,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (s.saving)
                                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            else
                                Text("Guardar cambios")
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (ui is ProfessionalScheduleUiState.Content) {
                ExtendedFloatingActionButton(
                    text = { Text("Agregar día") },
                    icon = { Icon(Icons.Filled.Add, null) },
                    onClick = { showAddDay = true }
                )
            }
        }
    ) { padding ->
        when (val s = ui) {
            ProfessionalScheduleUiState.Loading ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            is ProfessionalScheduleUiState.Error ->
                Column(
                    Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    ElevatedButton(onClick = { vm.load() }) { Text("Reintentar") }
                }

            is ProfessionalScheduleUiState.Content -> {
                val days = s.days
                val allUsedDays = days.map { it.day }.toSet()

                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(days, key = { it.day }) { dh ->
                        DayDropdownCard(
                            data = dh,
                            expanded = expandedByDay[dh.day] ?: false,
                            onToggle = { expandedByDay[dh.day] = !(expandedByDay[dh.day] ?: false) },
                            onRemoveHour = { hour -> vm.removeHour(dh.day, hour) },
                            onEditHours = { editDialogForDay = dh },
                            onDeleteDay = { vm.removeDay(dh.day) }
                        )
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }

                if (showAddDay) {
                    AddDayDialog(
                        usedDays = allUsedDays,
                        onDismiss = { showAddDay = false },
                        onPick = { day ->
                            showAddDay = false
                            vm.addDay(day)
                            expandedByDay[day] = true
                        }
                    )
                }

                editDialogForDay?.let { dh ->
                    HourPickerDialog(
                        day = dh.day,
                        initial = dh.hours.toSet(),
                        onDismiss = { editDialogForDay = null },
                        onConfirm = { picked ->
                            vm.setDayHours(dh.day, picked)
                            editDialogForDay = null
                        }
                    )
                }
            }
        }
    }
}

/* ------------------ UI helpers (todas @Composable) ------------------ */

@Composable
private fun DayDropdownCard(
    data: DayHours,
    expanded: Boolean,
    onToggle: () -> Unit,
    onRemoveHour: (Int) -> Unit,
    onEditHours: () -> Unit,
    onDeleteDay: () -> Unit
) {
    ElevatedCard(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(dayName(data.day), style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = onEditHours) { Text("Editar horas") }
                    TextButton(
                        onClick = onDeleteDay,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Eliminar día") }
                    IconButton(onClick = onToggle) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                if (data.hours.isEmpty()) {
                    Text("Sin horas registradas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        data.hours.forEach { h ->
                            AssistChip(
                                onClick = { onRemoveHour(h) },
                                label = { Text(hour12(h)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddDayDialog(usedDays: Set<Int>, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
    val options = (1..7).filterNot(usedDays::contains)
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(options.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { selected?.let(onPick) },
                enabled = selected != null
            ) { Text("Agregar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Agregar día") },
        text = {
            if (options.isEmpty()) {
                Text("Ya registraste todos los días disponibles.")
            } else {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = dayName(selected ?: options.first()),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Día") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        options.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(dayName(d)) },
                                onClick = { selected = d; expanded = false }
                            )
                        }
                    }
                }
            }
        }
    )
}


@Composable
private fun HourPickerDialog(
    day: Int,
    initial: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit
) {
    val picked = remember(day, initial) { mutableStateListOf<Int>().apply { addAll(initial.sorted()) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onConfirm(picked.toSet()) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Horas para ${dayName(day)}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Selecciona las horas (bloques de 1h):", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (0..23).forEach { h ->
                        val selected = picked.contains(h)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (selected) picked.remove(h) else picked.add(h)
                                picked.sort()
                            },
                            label = { Text(hour12(h)) }
                        )
                    }
                }
            }
        }
    )
}

/* ------------ utils ------------ */

private fun dayName(day: Int): String = when (day) {
    1 -> "Lunes"
    2 -> "Martes"
    3 -> "Miércoles"
    4 -> "Jueves"
    5 -> "Viernes"
    6 -> "Sábado"
    7 -> "Domingo"
    else -> "Día $day"
}

private fun hour12(h: Int): String {
    val hh = ((h + 11) % 12) + 1
    val am = if (h % 24 < 12) "AM" else "PM"
    return String.format("%02d:00 %s", hh, am)
}
