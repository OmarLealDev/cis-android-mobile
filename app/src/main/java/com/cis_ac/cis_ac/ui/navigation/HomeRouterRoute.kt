package com.cis_ac.cis_ac.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cis_ac.cis_ac.core.model.UserRole
import com.cis_ac.cis_ac.ui.feature.home.admin.AdminHomeNav
import com.cis_ac.cis_ac.ui.feature.home.admin.AdminHomeRoute
import com.cis_ac.cis_ac.ui.feature.home.patient.PatientHomeNav
import com.cis_ac.cis_ac.ui.feature.home.patient.PatientHomeRoute
import com.cis_ac.cis_ac.ui.feature.home.professional.ProfessionalHomeNav
import com.cis_ac.cis_ac.ui.feature.home.professional.ProfessionalHomeRoute

@Composable
fun HomeRouterRoute(
    vm: HomeRouterViewModel = viewModel(),
    onGoToLogin: () -> Unit,
    onNavigateFromPatient: (PatientHomeNav) -> Unit = {},
    onNavigateFromPro: (ProfessionalHomeNav) -> Unit = {},
    onNavigateFromAdmin: (AdminHomeNav) -> Unit = {},
    onAdminSignedOut: () -> Unit,

) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    when (val s = state) {
        is HomeRouterUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is HomeRouterUiState.NoSession -> {
            LaunchedEffect(Unit) { onGoToLogin() }
        }
        is HomeRouterUiState.Error -> {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                ElevatedButton(onClick = { vm.resolve() }) { Text("Reintentar") }
            }
        }
        is HomeRouterUiState.Ready -> {
            when (s.role) {
                UserRole.PATIENT -> PatientHomeRoute(onNavigate = onNavigateFromPatient)

                UserRole.PROFESSIONAL -> ProfessionalHomeRoute(
                    onNavigate = { dest: ProfessionalHomeNav ->
                        when (dest) {
                            ProfessionalHomeNav.Profile          -> onNavigateFromPro(dest)
                            ProfessionalHomeNav.Patients         -> onNavigateFromPro(dest)
                            ProfessionalHomeNav.Messages         -> onNavigateFromPro(dest)
                            ProfessionalHomeNav.Networks         -> onNavigateFromPro(dest)
                            ProfessionalHomeNav.Availability     -> onNavigateFromPro(dest)
                            ProfessionalHomeNav.Appointments     -> onNavigateFromPro(dest)
                            is ProfessionalHomeNav.AppointmentDetail -> onNavigateFromPro(dest)
                        }
                    }
                )

                UserRole.ADMIN -> AdminHomeRoute(
                    onNavigate = { onNavigateFromAdmin(it) },
                    onSignedOut = onAdminSignedOut
                )
                else -> Text("Rol no soportado: ${s.role}")
            }
        }
    }
}
