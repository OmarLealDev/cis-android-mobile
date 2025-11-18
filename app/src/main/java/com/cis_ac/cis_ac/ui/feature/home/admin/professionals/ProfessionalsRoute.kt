package com.cis_ac.cis_ac.ui.feature.home.admin.professionals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cis_ac.cis_ac.ui.feature.home.admin.professionals.components.ProfessionalItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalsRoute(
    vm: ProfessionalsViewModel = viewModel(),
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 2.dp) {
                Column {
                    CenterAlignedTopAppBar(
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
                        },
                        title = { Text("Profesionales") }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search
            OutlinedTextField(
                value = ui.query,
                onValueChange = vm::onQueryChange,
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                placeholder = { Text("Buscar profesional") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = ui.filter == ProFilter.ALL,
                    onClick = { vm.onFilterChange(ProFilter.ALL) },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = ui.filter == ProFilter.ACTIVE,
                    onClick = { vm.onFilterChange(ProFilter.ACTIVE) },
                    label = { Text("Activos") }
                )
                FilterChip(
                    selected = ui.filter == ProFilter.INACTIVE,
                    onClick = { vm.onFilterChange(ProFilter.INACTIVE) },
                    label = { Text("Inactivos") }
                )
            }

            Spacer(Modifier.height(12.dp))

            when {
                ui.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                ui.error.isNotEmpty() -> Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${ui.error}", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    ElevatedButton(onClick = { }) { Text("Reintentar") }
                }
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(ui.visible, key = { it.uid }) { pro ->
                        ProfessionalItemCard(
                            item = pro,
                            onOpenProfile = { onOpenProfile(pro.uid) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}
