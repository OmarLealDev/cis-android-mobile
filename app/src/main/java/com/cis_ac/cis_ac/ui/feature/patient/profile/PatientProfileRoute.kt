package com.cis_ac.cis_ac.ui.feature.patient.profile

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileRoute(
    vm: PatientProfileViewModel = viewModel(),
    onBack: () -> Unit,
    onOpenClinicalHistory: () -> Unit,
    onSignedOut: () -> Unit
) {
    val state by vm.ui.collectAsStateWithLifecycle()
    val signedOut by vm.signedOut.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.load() }

    LaunchedEffect(signedOut) {
        if (signedOut) onSignedOut()
    }

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
        }
    ) { padding ->
        when (val s = state) {
            PatientProfileUiState.Loading ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            is PatientProfileUiState.Error ->
                Column(
                    Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    ElevatedButton(onClick = { vm.load() }) { Text("Reintentar") }
                }

            is PatientProfileUiState.Content ->
                PatientProfileScreen(
                    state = s,
                    onEditToggle = vm::toggleEditing,
                    onNameChange = vm::onNameChange,
                    onEmailChange = vm::onEmailChange,
                    onPhoneChange = vm::onPhoneChange,
                    onCancel = vm::cancelEdit,
                    onSave = vm::save,
                    onOpenClinicalHistory = onOpenClinicalHistory,
                    onSignOut = vm::signOut,
                    contentPadding = padding
                )
        }
    }
}
