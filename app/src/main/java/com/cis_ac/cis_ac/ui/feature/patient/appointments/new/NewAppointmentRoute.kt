package com.cis_ac.cis_ac.ui.feature.patient.appointments.new

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAppointmentRoute(
    preselectedDisciplineName: String? = null,
    preselectedProfessionalId: String? = null,
    preselectedProfessionalName: String? = null,
    vm: NewAppointmentViewModel = viewModel(),
    onBack: () -> Unit,
    onCreatedWithMessage: (String) -> Unit
) {
    val ui by vm.ui
    var navigating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val disc = preselectedDisciplineName?.let { name ->
            ui.disciplines.find { it.name == name }
        }
        vm.preselect(disc, preselectedProfessionalId, preselectedProfessionalName)
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(ui.success) {
        if (ui.success) {
            snackbarHostState.showSnackbar("¡Cita agendada correctamente!")
            vm.consumeSuccess()
            onCreatedWithMessage("¡Cita agendada correctamente!")
        }
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 2.dp) {
                Column {
                    CenterAlignedTopAppBar(
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                        title = { Text("Agendar cita") }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            var discExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = discExpanded,
                onExpandedChange = { discExpanded = !discExpanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    value = ui.selectedDiscipline?.spanishName ?: "",
                    onValueChange = {},
                    label = { Text("Seleccionar disciplina") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = discExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = discExpanded,
                    onDismissRequest = { discExpanded = false }
                ) {
                    ui.disciplines.forEach { d ->
                        DropdownMenuItem(
                            text = { Text(d.spanishName) },
                            onClick = {
                                vm.onDisciplineSelected(d)
                                discExpanded = false
                            }
                        )
                    }
                }
            }

            var proExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = proExpanded,
                onExpandedChange = {
                    if (ui.selectedDiscipline != null) proExpanded = !proExpanded
                }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    value = ui.selectedProfessional?.name ?: "",
                    onValueChange = {},
                    label = { Text("Seleccionar profesional") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = proExpanded) },
                    enabled = ui.selectedDiscipline != null
                )
                ExposedDropdownMenu(
                    expanded = proExpanded,
                    onDismissRequest = { proExpanded = false }
                ) {
                    ui.professionals.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name) },
                            onClick = {
                                vm.onProfessionalSelected(p.uid, p.name)
                                proExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = ui.selectedEpochDay?.let { LocalDate.ofEpochDay(it).toString() } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha de cita") },
                modifier = Modifier.fillMaxWidth(),
                enabled = ui.selectedProfessional != null,
                trailingIcon = {
                    TextButton(
                        onClick = {
                            if (ui.selectedProfessional != null && ui.enabledDays.isNotEmpty()) {
                                showDatePicker = true
                            }
                        },
                        enabled = ui.selectedProfessional != null && ui.enabledDays.isNotEmpty()
                    ) { Text("Elegir") }
                }
            )

            if (showDatePicker) {
                val today = remember { LocalDate.now(ZoneId.systemDefault()) }

                val selectable = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val date = Instant.ofEpochMilli(utcTimeMillis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()

                        val isNotPast = !date.isBefore(today)
                        val matchesProDay = ui.enabledDays.contains(date.dayOfWeek.value)

                        return isNotPast && matchesProDay
                    }
                }

                val dpState = rememberDatePickerState(selectableDates = selectable)

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                dpState.selectedDateMillis?.let { ms ->
                                    val picked = Instant.ofEpochMilli(ms)
                                        .atZone(ZoneOffset.UTC)
                                        .toLocalDate()
                                    vm.onDateSelected(picked)
                                }
                                showDatePicker = false
                            },
                            enabled = dpState.selectedDateMillis != null
                        ) { Text("Aceptar") }
                    },
                    dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
                ) {
                    DatePicker(
                        state = dpState,
                        showModeToggle = false
                    )
                }
            }

            if (ui.selectedEpochDay != null && ui.availableHours.isNotEmpty()) {
                Text("Seleccionar hora", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ui.availableHours.forEach { h ->
                        val label = String.format("%02d:00", h)
                        val selected = ui.selectedHour == h
                        val disabled = h in ui.disabledHours

                        FilterChip(
                            selected = selected,
                            onClick = { if (!disabled) vm.onHourSelected(h) },
                            enabled = !disabled,
                            label = { Text(label) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = ui.notes,
                onValueChange = vm::onNotesChange,
                label = { Text("Motivo de la consulta (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                minLines = 3
            )

            if (ui.error.isNotEmpty()) {
                Text(ui.error, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    navigating = true
                    vm.submit(
                        onSuccess = {
                            onCreatedWithMessage("¡Cita agendada correctamente!")
                        },
                        onError = {
                            navigating = false
                        }
                    )
                },
                enabled = !ui.isLoading && vm.canSubmit() && !navigating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (ui.isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text("Confirmar cita")
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
