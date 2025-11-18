package com.cis_ac.cis_ac.ui.feature.professional.appointments

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalAppointmentsRoute(
    navController: NavController,
    vm: ProfessionalAppointmentsViewModel = viewModel(),
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit
) {
    val state by vm.ui.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val showUpcoming by vm.showUpcoming.collectAsStateWithLifecycle()
    val showPast     by vm.showPast.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.load() }

    LaunchedEffect(navController.currentBackStackEntry) {
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        val msg: String? = handle.get<String>("pro_snackbar_msg")
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            handle.set("pro_snackbar_msg", null as String?)
        }
        val refresh: Boolean? = handle.get<Boolean>("pro_snackbar_refresh")
        if (refresh == true) {
            vm.load()
            handle.set("pro_snackbar_refresh", false)
        }
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 2.dp) {
                Column {
                    CenterAlignedTopAppBar(
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                        title = { Text("Gestionar citas") }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        when (val s = state) {
            ProfessionalAppointmentsUiState.Loading ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            is ProfessionalAppointmentsUiState.Error ->
                Column(
                    Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { vm.load() }) { Text("Reintentar") }
                }

            is ProfessionalAppointmentsUiState.Content ->
                ProfessionalAppointmentsScreen(
                    upcoming = s.upcoming,
                    past = s.past,
                    showUpcoming = showUpcoming,
                    showPast = showPast,
                    onToggleUpcoming = vm::toggleUpcoming,
                    onTogglePast = vm::togglePast,
                    onOpen = onOpenDetail,
                    onApprove = onOpenDetail,
                    contentPadding = padding
                )
        }
    }
}
