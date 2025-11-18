package com.cis_ac.cis_ac.ui.feature.patient.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun PatientProfileScreen(
    state: PatientProfileUiState.Content,
    onEditToggle: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onOpenClinicalHistory: () -> Unit,
    onSignOut: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    var confirmSignOut by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(96.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, null)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(state.fullName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text("Paciente", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Mis registros", style = MaterialTheme.typography.titleMedium)
            ElevatedCard(
                onClick = onOpenClinicalHistory,
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.elevatedCardElevation(1.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Historial clínico", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.KeyboardArrowRight, null)
                }
            }
        }

        Row {
            FilledTonalButton(
                onClick = onEditToggle,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Filled.Edit, null)
                Spacer(Modifier.width(8.dp))
                Text(if (state.isEditing) "Dejar de editar" else "Editar")
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LabeledTextField(
                label = "Nombre completo",
                value = state.fullName,
                onValue = onNameChange,
                enabled = state.isEditing
            )
            LabeledTextField(
                label = "Correo electrónico",
                value = state.email,
                onValue = onEmailChange,
                enabled = false
            )
            LabeledTextField(
                label = "Teléfono",
                value = state.phone,
                onValue = onPhoneChange,
                enabled = state.isEditing
            )
        }

        if (state.isEditing) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Cancelar cambios") }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSaving,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (state.isSaving) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    else Text("Guardar cambios")
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Cuenta", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = { confirmSignOut = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cerrar sesión") }
        }

        Spacer(Modifier.height(8.dp))
    }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que deseas cerrar tu sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmSignOut = false
                    onSignOut()
                }) { Text("Sí, cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { confirmSignOut = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValue: (String) -> Unit,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(
            value = value,
            onValueChange = onValue,
            enabled = enabled,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
