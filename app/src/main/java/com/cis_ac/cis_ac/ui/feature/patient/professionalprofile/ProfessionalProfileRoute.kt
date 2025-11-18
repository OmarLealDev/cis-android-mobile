package com.cis_ac.cis_ac.ui.feature.patient.professionalprofile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalProfileRoute(
    uid: String,
    vm: ProfessionalProfileViewModel = viewModel(),
    onBack: () -> Unit,
    onChat: ((String) -> Unit)? = null
) {
    val state by vm.ui.collectAsStateWithLifecycle()

    LaunchedEffect(uid) { vm.load(uid) }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 2.dp) {
                Column {
                    CenterAlignedTopAppBar(
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                        title = { Text("Perfil del profesional") }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
        }
    ) { padding ->
        when (val s = state) {
            ProfessionalProfileUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) { CircularProgressIndicator() }

            is ProfessionalProfileUiState.Error -> Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                ElevatedButton(onClick = { vm.load(uid) }) { Text("Reintentar") }
            }

            is ProfessionalProfileUiState.Content -> ProfessionalProfileScreen(
                data = s,
                contentPadding = padding,
                onChat = onChat?.let { callback -> { callback(uid) } }
            )
        }
    }
}
