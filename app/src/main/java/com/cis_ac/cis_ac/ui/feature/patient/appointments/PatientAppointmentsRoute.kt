package com.cis_ac.cis_ac.ui.feature.patient.appointments

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientAppointmentsRoute(
    navController: NavHostController,
    vm: PatientAppointmentsViewModel = viewModel(factory = PatientAppointmentsViewModel.factory()),
    onBack: () -> Unit,
    onNewAppointment: () -> Unit,
    onOpenAppointment: (String) -> Unit = {}
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.load() }

    val showUpcoming by vm.showUpcoming.collectAsStateWithLifecycle()
    val showPast     by vm.showPast.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var consumed by remember { mutableStateOf(false) }
    LaunchedEffect(navController) {
        val backEntry = navController.currentBackStackEntry
        val message: String? = backEntry?.savedStateHandle?.get("snackbar_message")
        if (!message.isNullOrBlank() && !consumed) {
            snackbarHostState.showSnackbar(message)
            backEntry?.savedStateHandle?.set("snackbar_message", null as String?)
            consumed = true
        }
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 2.dp) {
                Column {
                    CenterAlignedTopAppBar(
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                        title = { Text("Citas") }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Nueva cita") },
                icon = { Icon(Icons.Filled.Add, null) },
                onClick = {
                    consumed = false
                    onNewAppointment()
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        when (val s = ui) {
            PatientAppointmentsUiState.Loading ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            is PatientAppointmentsUiState.Error ->
                Column(
                    Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { vm.load() }) { Text("Reintentar") }
                }

            is PatientAppointmentsUiState.Content ->
                PatientAppointmentsScreen(
                    upcoming = s.upcoming,
                    past = s.past,
                    showUpcoming = showUpcoming,
                    showPast = showPast,
                    onToggleUpcoming = vm::toggleUpcoming,
                    onTogglePast = vm::togglePast,
                    onOpen = {  },
                    contentPadding = padding
                )
        }
    }
}
