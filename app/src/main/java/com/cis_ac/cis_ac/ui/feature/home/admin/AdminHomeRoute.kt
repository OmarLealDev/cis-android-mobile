package com.cis_ac.cis_ac.ui.feature.home.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cis_ac.cis_ac.ui.feature.shared.BottomBarItem
import com.cis_ac.cis_ac.ui.feature.shared.UserBottomBar
import kotlinx.coroutines.launch

enum class AdminHomeNav { Home, Professionals, Requests }

@Composable
fun AdminHomeRoute(
    vm: AdminHomeViewModel = viewModel(),
    onNavigate: (AdminHomeNav) -> Unit,
    onSignedOut: () -> Unit = {}
) {
    val state = vm.uiState.collectAsStateWithLifecycle().value

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) vm.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val tabs = listOf(
        BottomBarItem(id = "home", label = "Inicio", icon = Icons.Filled.Home),
        BottomBarItem(id = "professionals", label = "Profesionales", icon = Icons.Filled.Groups),
        BottomBarItem(id = "requests", label = "Solicitudes", icon = Icons.Filled.Inbox),
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // <-- para mostrar snackbar desde callbacks
    var showSignOutDialog by remember { mutableStateOf(false) }
    var signingOut by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 2.dp) {
                Column {
                    val name = (state as? AdminHomeUiState.Content)?.adminName ?: "Administrador"
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Bienvenido de nuevo,",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showSignOutDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Logout,
                                contentDescription = "Cerrar sesión"
                            )
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 1.dp)
                }
            }
        },
        bottomBar = {
            UserBottomBar(
                items = tabs,
                selectedId = "home",
                onSelect = { item ->
                    when (item.id) {
                        "home" -> Unit
                        "professionals" -> onNavigate(AdminHomeNav.Professionals)
                        "requests" -> onNavigate(AdminHomeNav.Requests)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        AdminHomeScreen(
            state = state,
            onOpenProfessionals = { onNavigate(AdminHomeNav.Professionals) },
            onOpenRequests = { onNavigate(AdminHomeNav.Requests) },
            onRefresh = { vm.refresh() },
            contentPadding = padding
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { if (!signingOut) showSignOutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que deseas cerrar la sesión?") },
            confirmButton = {
                TextButton(
                    enabled = !signingOut,
                    onClick = {
                        signingOut = true
                        vm.signOut(
                            onSuccess = {
                                signingOut = false
                                showSignOutDialog = false
                                onSignedOut()
                            },
                            onError = { msg ->
                                signingOut = false
                                showSignOutDialog = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msg.ifBlank { "No se pudo cerrar sesión" }
                                    )
                                }
                            }
                        )
                    }
                ) {
                    if (signingOut) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Cerrar sesión")
                    }
                }
            },
            dismissButton = {
                TextButton(enabled = !signingOut, onClick = { showSignOutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
