@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.cis_ac.cis_ac.ui.feature.professional.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cis_ac.cis_ac.core.model.Discipline
import com.cis_ac.cis_ac.core.model.Modality
import com.cis_ac.cis_ac.core.model.Population
import com.cis_ac.cis_ac.core.model.Sessions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalProfileRoute(
    vm: ProfessionalProfileViewModel = viewModel(),
    onBack: () -> Unit,
    onSignedOut: () -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val msg by vm.message.collectAsStateWithLifecycle(initialValue = null)

    val signedOut by vm.signedOut.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(msg) { msg?.let { snackbar.showSnackbar(it); vm.consumeMessage() } }

    LaunchedEffect(signedOut) {
        if (signedOut) onSignedOut()
    }

    val bottomPaddingWhenEditing = 88.dp

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 2.dp) {
                Column {
                    CenterAlignedTopAppBar(
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                        title = { Text("Mi perfil") }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            val s = state
            if (s is ProfessionalProfileUiState.Content && s.editing) {
                Surface(tonalElevation = 2.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = vm::cancelEdit,
                            enabled = !s.saving,
                            modifier = Modifier.weight(1f)
                        ) { Text("Cancelar") }

                        Button(
                            onClick = vm::save,
                            enabled = !s.saving,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (s.saving) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            else Text("Guardar")
                        }
                    }
                }
            }
        }
    ) { padding ->
        when (val s = state) {
            ProfessionalProfileUiState.Loading ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            is ProfessionalProfileUiState.Error ->
                Column(
                    Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    ElevatedButton(onClick = { vm.load() }) { Text("Reintentar") }
                }

            is ProfessionalProfileUiState.Content -> {
                var confirmSignOut by remember { mutableStateOf(false) }

                val contentBottomPad = if (s.editing) bottomPaddingWhenEditing else 0.dp

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = contentBottomPad),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier.size(80.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Person, null) } }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            s.fullName.ifBlank { "Profesional" },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (s.verified) {
                            AssistChip(
                                onClick = { }, enabled = false,
                                label = { Text("Verificado") },
                                leadingIcon = { Icon(Icons.Filled.Verified, null) }
                            )
                        }
                    }

                    Text("Mis registros", style = MaterialTheme.typography.titleMedium)

                    OutlinedCard(
                        onClick = {
                            s.cvUrl?.let { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
                                ?: vm.toast("Aún no has cargado tu CV")
                        },
                        enabled = s.cvUrl != null
                    ) {
                        ListItem(
                            headlineContent = { Text("CV (descargar)") },
                            supportingContent = { Text(if (s.cvUrl != null) "Abrir enlace" else "No disponible") },
                            leadingContent = { Icon(Icons.Filled.Description, null) }
                        )
                    }

                    OutlinedCard(
                        onClick = {
                            s.licenseUrl?.let { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
                                ?: vm.toast("Aún no has cargado tu cédula")
                        },
                        enabled = s.licenseUrl != null
                    ) {
                        ListItem(
                            headlineContent = { Text("Cédula (descargar)") },
                            supportingContent = { Text(if (s.licenseUrl != null) "Abrir enlace" else "No disponible") },
                            leadingContent = { Icon(Icons.Filled.Description, null) }
                        )
                    }

                    if (!s.editing) {
                        ElevatedButton(onClick = vm::toggleEditing) { Text("Editar") }
                    }

                    /* ----------- Datos Personales ----------- */
                    Text("Datos personales", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = s.fullName,
                        onValueChange = vm::onFullNameChange,
                        label = { Text("Nombre completo") },
                        enabled = s.editing,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )

                    OutlinedTextField(
                        value = s.email,
                        onValueChange = {},
                        label = { Text("Correo electrónico") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = s.phone,
                        onValueChange = vm::onPhoneChange,
                        label = { Text("Teléfono") },
                        enabled = s.editing,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    OutlinedTextField(
                        value = s.dob,
                        onValueChange = vm::onDobChange,
                        label = { Text("Fecha de nacimiento") },
                        enabled = s.editing,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )


                    /* ----------- Datos Profesionales ----------- */
                    Text("Datos profesionales", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = s.discipline?.spanishName ?: "",
                        onValueChange = {},
                        label = { Text("Disciplina principal") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = s.licenseNumber,
                        onValueChange = vm::onLicenseChange,
                        label = { Text("Número de cédula") },
                        enabled = s.editing,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = s.speciality,
                        onValueChange = vm::onSpecialityChange,
                        label = { Text("Especialidad") },
                        enabled = s.editing,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (s.discipline == Discipline.PSYCHOLOGY) {
                        OutlinedTextField(
                            value = s.approach,
                            onValueChange = vm::onApproachChange,
                            label = { Text("Enfoque") },
                            enabled = s.editing,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = s.topics,
                        onValueChange = vm::onTopicsChange,
                        label = { Text("Temas (separados por coma, opcional)") },
                        enabled = s.editing,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = s.expertiz,
                        onValueChange = vm::onExpertizChange,
                        label = { Text("Experiencia / Expertise") },
                        enabled = s.editing,
                        modifier = Modifier.fillMaxWidth()
                    )

                    /* ----------- Preferencias de atención ----------- */
                    Text("Modalidad, tipo de sesión y población", style = MaterialTheme.typography.titleMedium)

                    EnumMultiSelectRow(
                        title = "Modalidades",
                        all = Modality.values().toList(),
                        selected = s.modalities,
                        enabled = s.editing,
                        labelFor = { it.spanishName },
                        onToggle = vm::toggleModality
                    )

                    EnumMultiSelectRow(
                        title = "Tipos de sesión",
                        all = Sessions.values().toList(),
                        selected = s.sessionTypes,
                        enabled = s.editing,
                        labelFor = { it.spanishName },
                        onToggle = vm::toggleSession
                    )

                    EnumMultiSelectRow(
                        title = "Poblaciones",
                        all = Population.values().toList(),
                        selected = s.populations,
                        enabled = s.editing,
                        labelFor = { it.spanishName },
                        onToggle = vm::togglePopulation
                    )

                    /* ----------- Presentación ----------- */
                    Text("Semblanza", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = s.semblance,
                        onValueChange = vm::onSemblanceChange,
                        label = { Text("Cuéntale a tus pacientes sobre ti") },
                        enabled = s.editing,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )

                    AssistChip(
                        onClick = { /* navegar a disponibilidad */ },
                        enabled = false,
                        label = { Text("La disponibilidad (agenda) se edita en otra pantalla") }
                    )

                    Spacer(Modifier.height(8.dp))
                    Text("Cuenta", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = { confirmSignOut = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cerrar sesión") }

                    Spacer(Modifier.height(6.dp))
                }

                if (confirmSignOut) {
                    AlertDialog(
                        onDismissRequest = { confirmSignOut = false },
                        title = { Text("Cerrar sesión") },
                        text = { Text("¿Seguro que deseas cerrar tu sesión?") },
                        confirmButton = {
                            TextButton(onClick = {
                                confirmSignOut = false
                                vm.signOut()
                            }) { Text("Sí, cerrar sesión") }
                        },
                        dismissButton = {
                            TextButton(onClick = { confirmSignOut = false }) { Text("Cancelar") }
                        }
                    )
                }
            }
        }
    }
}

/* ================== PICKERS Y HELPERS UI (sin cambios) ================== */

@Composable
private fun <T> EnumMultiSelectRow(
    title: String,
    all: List<T>,
    selected: Set<T>,
    enabled: Boolean,
    labelFor: (T) -> String,
    onToggle: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            all.forEach { item ->
                val isSel = item in selected
                FilterChip(
                    selected = isSel,
                    onClick = { if (enabled) onToggle(item) },
                    enabled = enabled,
                    label = { Text(labelFor(item)) }
                )
            }
        }
    }
}