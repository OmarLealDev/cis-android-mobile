package com.cis_ac.cis_ac.ui.feature.patient.professionals.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cis_ac.cis_ac.core.model.Discipline
import com.cis_ac.cis_ac.core.model.label

@Composable
fun FilterChipsRow(
    selectedDiscipline: Discipline?,
    onDiscipline: (Discipline?) -> Unit,
    selectedPopulation: String?,
    onPopulation: (String?) -> Unit,
    selectedModality: String?,
    onModality: (String?) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf("disc", "pop", "mod", "reset")

    LazyRow(
        modifier = modifier,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(keys) { key ->
            when (key) {
                "disc" -> DisciplineFilterChip(
                    selected = selectedDiscipline,
                    onSelect = onDiscipline
                )

                "pop" -> ExposedDropdownFilterChip(
                    current = selectedPopulation ?: "Tipo de población",
                    items = listOf("Todas", "Infantes", "Adolescentes", "Adultos", "Adultos mayores"),
                    onSelectedIndex = { i ->
                        onPopulation(listOf<String?>(null, "Infantes", "Adolescentes", "Adultos", "Adultos mayores")[i])
                    }
                )

                "mod" -> ExposedDropdownFilterChip(
                    current = selectedModality ?: "Modalidad",
                    items = listOf("Todas", "Presencial", "En línea", "Domicilio"),
                    onSelectedIndex = { i ->
                        onModality(listOf<String?>(null, "Presencial", "En línea", "Domicilio")[i])
                    }
                )

                "reset" -> AssistChip(
                    onClick = onReset,
                    leadingIcon = { Icon(Icons.Outlined.Refresh, null) },
                    label = { Text("Restablecer") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisciplineFilterChip(
    selected: Discipline?,
    onSelect: (Discipline?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val all = Discipline.entries

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        FilterChip(
            selected = expanded,
            onClick = { expanded = true },
            label = {
                Text(
                    selected?.label() ?: "Especialidad",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true, selected = false,
                borderColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedBorderColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Todas") }, onClick = {
                onSelect(null); expanded = false
            })
            all.forEach { d ->
                DropdownMenuItem(text = { Text(d.label()) }, onClick = {
                    onSelect(d); expanded = false
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownFilterChip(
    current: String,
    items: List<String>,
    onSelectedIndex: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        FilterChip(
            selected = expanded,
            onClick = { expanded = true },
            label = {
                Text(
                    current,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true, selected = false,
                borderColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedBorderColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEachIndexed { i, text ->
                DropdownMenuItem(text = { Text(text) }, onClick = {
                    onSelectedIndex(i); expanded = false
                })
            }
        }
    }
}
