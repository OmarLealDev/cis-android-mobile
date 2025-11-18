package com.cis_ac.cis_ac.ui.feature.patient.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.cis_ac.cis_ac.core.model.history.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHistoryScreen(
    state: PatientHistoryUiState,
    onBack: () -> Unit,
    onToggleAllergies: () -> Unit,
    onEditAllergy: (String) -> Unit,
    onAddNote: () -> Unit,
    onOpenSection: (Section) -> Unit,
    onSave: () -> Unit,

    onToggleDiagnoses: () -> Unit = {},
    onToggleTreatments: () -> Unit = {},
    onToggleMeds: () -> Unit = {},
    onAddDiagnoseNote: () -> Unit = {},
    onAddTreatmentNote: () -> Unit = {},
    onAddMedNote: () -> Unit = {},
    onAddAllergyNote: () -> Unit = {},

    onToggleGeneral: () -> Unit = {},
    onGeneralChange: (GeneralInfo) -> Unit = {},
    onRequestEditNote: (HistoryEntry) -> Unit = {},
    onCommitNote: (HistoryEntry, String, String) -> Unit = { _, _, _ -> },
    onDismissEditor: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    generalEditable: Boolean = true
) {
    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 2.dp) {
                Column {
                    CenterAlignedTopAppBar(
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
                        },
                        title = { Text("Historial clínico") }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            state.loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            state.error.isNotEmpty() -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    ElevatedButton(onClick = { /* vm.load() */ }) { Text("Reintentar") }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        GeneralSectionCard(
                            expanded = state.generalExpanded,
                            info = state.general,
                            onToggle = onToggleGeneral,
                            onChange = onGeneralChange,
                            enabled = generalEditable
                        )
                    }

                    item {
                        CollapsibleSection(
                            title = "Diagnósticos",
                            expanded = state.diagnosesExpanded,
                            items = state.diagnoses,
                            onToggle = onToggleDiagnoses,
                            onAddNote = onAddDiagnoseNote,
                            onEdit = onRequestEditNote
                        )
                    }

                    item {
                        CollapsibleSection(
                            title = "Tratamientos",
                            expanded = state.treatmentsExpanded,
                            items = state.treatments,
                            onToggle = onToggleTreatments,
                            onAddNote = onAddTreatmentNote,
                            onEdit = onRequestEditNote
                        )
                    }

                    item {
                        CollapsibleSection(
                            title = "Medicamentos",
                            expanded = state.medsExpanded,
                            items = state.medications,
                            onToggle = onToggleMeds,
                            onAddNote = onAddMedNote,
                            onEdit = onRequestEditNote
                        )
                    }

                    item {
                        CollapsibleSection(
                            title = "Alergias",
                            expanded = state.allergiesExpanded,
                            items = state.allergies,
                            onToggle = onToggleAllergies,
                            onAddNote = onAddAllergyNote,
                            onEdit = onRequestEditNote
                        )
                    }

                    item {
                        if (generalEditable) {
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = onSave,
                                enabled = state.canSave,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("Guardar cambios") }
                            Spacer(Modifier.height(8.dp))
                        } else {
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                state.editingNote?.let { entry ->
                    NoteEditorDialog(
                        original = entry,
                        onDismiss = onDismissEditor,
                        onSave = { newDate, newText -> onCommitNote(entry, newDate, newText) }
                    )
                }
            }
        }
    }
}


@Composable
private fun GeneralSectionCard(
    expanded: Boolean,
    info: GeneralInfo,
    onToggle: () -> Unit,
    onChange: (GeneralInfo) -> Unit,
    enabled: Boolean
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Datos generales",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }

            if (expanded) {
                Divider()
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = info.ageYears?.toString().orEmpty(),
                            onValueChange = { v -> onChange(info.copy(ageYears = v.toIntOrNull())) },
                            label = { Text("Edad (años)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            enabled = enabled
                        )
                        OutlinedTextField(
                            value = info.weightKg?.toString().orEmpty(),
                            onValueChange = { v -> onChange(info.copy(weightKg = v.toFloatOrNull())) },
                            label = { Text("Peso (kg)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            enabled = enabled
                        )
                        OutlinedTextField(
                            value = info.heightCm?.toString().orEmpty(),
                            onValueChange = { v -> onChange(info.copy(heightCm = v.toFloatOrNull())) },
                            label = { Text("Estatura (cm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            enabled = enabled
                        )
                    }

                    OutlinedTextField(
                        value = info.bloodType.orEmpty(),
                        onValueChange = { v -> onChange(info.copy(bloodType = v.uppercase())) },
                        label = { Text("Tipo de sangre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled
                    )

                    OutlinedTextField(
                        value = info.chronicConditions.joinToString(", "),
                        onValueChange = { v ->
                            onChange(
                                info.copy(
                                    chronicConditions = v.split(',')
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }
                                )
                            )
                        },
                        label = { Text("Condiciones crónicas (coma-separado)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled
                    )
                    OutlinedTextField(
                        value = info.surgeries.joinToString(", "),
                        onValueChange = { v ->
                            onChange(
                                info.copy(
                                    surgeries = v.split(',')
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }
                                )
                            )
                        },
                        label = { Text("Cirugías (coma-separado)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled
                    )

                    Text("Hábitos", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledCheckbox("Fuma", info.habits.smoker) {
                            if (enabled) onChange(info.copy(habits = info.habits.copy(smoker = it)))
                        }
                        LabeledCheckbox("Alcohol", info.habits.alcoholUse) {
                            if (enabled) onChange(info.copy(habits = info.habits.copy(alcoholUse = it)))
                        }
                    }
                    OutlinedTextField(
                        value = info.habits.exerciseFreq,
                        onValueChange = { v -> onChange(info.copy(habits = info.habits.copy(exerciseFreq = v))) },
                        label = { Text("Frecuencia de ejercicio") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled
                    )

                    Text("Contacto de emergencia", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = info.emergencyContact.name,
                        onValueChange = { v ->
                            onChange(info.copy(emergencyContact = info.emergencyContact.copy(name = v)))
                        },
                        label = { Text("Nombre completo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled
                    )
                    OutlinedTextField(
                        value = info.emergencyContact.phone,
                        onValueChange = { v ->
                            onChange(info.copy(emergencyContact = info.emergencyContact.copy(phone = v)))
                        },
                        label = { Text("Teléfono") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled
                    )
                    OutlinedTextField(
                        value = info.emergencyContact.relation,
                        onValueChange = { v ->
                            onChange(info.copy(emergencyContact = info.emergencyContact.copy(relation = v)))
                        },
                        label = { Text("Relación") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled
                    )
                }
            }
        }
    }
}

@Composable
private fun LabeledCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(6.dp))
        Text(label)
    }
}


@Composable
private fun CollapsibleSection(
    title: String,
    expanded: Boolean,
    items: List<HistoryEntry>,
    onToggle: () -> Unit,
    onAddNote: () -> Unit,
    onEdit: (HistoryEntry) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }

            if (expanded) {
                Divider()
                if (items.isEmpty()) {
                    Text(
                        "Sin registros",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                } else {
                    items.forEach { item ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 70.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                if (item.date.isNotBlank()) {
                                    Text(
                                        item.date,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    item.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                val created = "por ${item.createdBy.name}"
                                val edited = item.updatedBy?.let { " — editado por ${it.name}" }.orEmpty()
                                Text(
                                    text = "$created$edited",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onEdit(item) }) { Icon(Icons.Filled.Edit, null) }
                        }
                        Divider()
                    }
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedButton(
                        onClick = onAddNote,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Añadir nota") }
                }
            }
        }
    }
}

/* ========== Note editor dialog ========== */

@Composable
private fun NoteEditorDialog(
    original: HistoryEntry,
    onDismiss: () -> Unit,
    onSave: (newDate: String, newText: String) -> Unit
) {
    var date by remember(original.id) { mutableStateOf(original.date) }
    var text by remember(original.id) { mutableStateOf(original.text) }

    LaunchedEffect(original.id, original.date, original.text) {
        date = original.date
        text = original.text
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar nota") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha (dd/MM/yyyy)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Descripción") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(date, text) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
