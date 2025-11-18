package com.cis_ac.cis_ac.ui.feature.patient.professionals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cis_ac.cis_ac.ui.feature.patient.professionals.components.FilterChipsRow
import com.cis_ac.cis_ac.ui.feature.patient.professionals.components.ProfessionalCardForPatient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfessionalsRoute(
    vm: PatientProfessionalsViewModel = viewModel(),
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenSchedule: (PatientProfessionalItem) -> Unit,
    onOpenChat: (PatientProfessionalItem) -> Unit
) {
    val uiState: PatientProfessionalsUiState by vm.ui.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 2.dp) {
                Column {
                    CenterAlignedTopAppBar(
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                        title = { Text("Profesionales") }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
        }
    ) { padding ->
        when (val s = uiState) {
            PatientProfessionalsUiState.Loading ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            is PatientProfessionalsUiState.Error ->
                Column(
                    Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    ElevatedButton(onClick = { vm.reload() }) { Text("Reintentar") }
                }

            is PatientProfessionalsUiState.Content -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = s.filters.query,
                        onValueChange = vm::onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar profesional") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Filled.Search, null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    FilterChipsRow(
                        selectedDiscipline = s.filters.discipline,
                        onDiscipline = vm::onDisciplineChange,
                        selectedPopulation = s.filters.populationType,
                        onPopulation = vm::onPopulationChange,
                        selectedModality = s.filters.modality,
                        onModality = vm::onModalityChange,
                        onReset = vm::resetFilters
                    )

                    Spacer(Modifier.height(10.dp))

                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(s.items, key = { it.uid }) { item ->
                            ProfessionalCardForPatient(
                                item = item,
                                onProfile = { onOpenProfile(item.uid) },
                                onSchedule = { onOpenSchedule(item) },
                                onChat = { onOpenChat(item) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
