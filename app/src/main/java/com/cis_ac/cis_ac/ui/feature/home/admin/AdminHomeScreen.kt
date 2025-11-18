package com.cis_ac.cis_ac.ui.feature.home.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cis_ac.cis_ac.ui.feature.home.admin.components.AdminStatCard
import com.cis_ac.cis_ac.ui.theme.Quaternary

@Composable
fun AdminHomeScreen(
    state: AdminHomeUiState,
    onOpenProfessionals: () -> Unit,
    onOpenRequests: () -> Unit,
    onRefresh: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    when (state) {
        is AdminHomeUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is AdminHomeUiState.Error -> Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
            ElevatedButton(onClick = onRefresh) { Text("Reintentar") }
        }
        is AdminHomeUiState.Content -> {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Resumen",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(12.dp))

                AdminStatCard(
                    title = "Profesionales activos",
                    value = state.activeCount,
                    subtitle = "Total de profesionales registrados",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    onClick = onOpenProfessionals
                )
                Spacer(Modifier.height(16.dp))

                AdminStatCard(
                    title = "Solicitudes pendientes",
                    value = state.pendingCount,
                    subtitle = "Total de solicitudes por revisar",
                    containerColor = Quaternary,
                    onClick = onOpenRequests
                )
            }
        }
    }
}
