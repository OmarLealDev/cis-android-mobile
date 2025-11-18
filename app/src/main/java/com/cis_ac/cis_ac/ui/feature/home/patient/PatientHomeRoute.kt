package com.cis_ac.cis_ac.ui.feature.home.patient

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cis_ac.cis_ac.ui.feature.home.patient.reviews.ReviewPromptDialog
import com.cis_ac.cis_ac.ui.feature.shared.BottomBarItem
import com.cis_ac.cis_ac.ui.feature.shared.UserBottomBar
private val patientTabs = listOf(
    BottomBarItem(id = "home", label = "Inicio", icon = Icons.Filled.Home),
    BottomBarItem(id = "professionals",  label = "Profesionales",  icon = Icons.Filled.Groups),
    BottomBarItem(id = "appointments",   label = "Citas",          icon = Icons.Filled.CalendarMonth),
    BottomBarItem(id = "messages",       label = "Mensajes",       icon = Icons.Filled.ChatBubbleOutline),
    BottomBarItem(id = "networks",       label = "Redes",          icon = Icons.Filled.MenuBook),
)
@Composable
fun PatientHomeRoute(
    vm: PatientHomeViewModel = viewModel(),
    onNavigate: (PatientHomeNav) -> Unit
) {
    val state = vm.uiState.collectAsStateWithLifecycle().value

    val reviewVm: com.cis_ac.cis_ac.ui.feature.home.patient.reviews.PatientReviewPromptViewModel = viewModel()
    val reviewUi by reviewVm.ui.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { reviewVm.load() }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(reviewVm) {
        reviewVm.events.collect { ev ->
            when (ev) {
                is com.cis_ac.cis_ac.ui.feature.home.patient.reviews.ReviewEvent.Toast ->
                    snackbarHostState.showSnackbar(ev.message)
            }
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            UserBottomBar(
                items = patientTabs,
                selectedId = "home",
                onSelect = { item ->
                    when (item.id) {
                        "home" -> Unit
                        "professionals" -> onNavigate(PatientHomeNav.ProfessionalsList)
                        "appointments"  -> onNavigate(PatientHomeNav.AppointmentsTab)
                        "messages"      -> onNavigate(PatientHomeNav.MessagesTab)
                        "networks"      -> onNavigate(PatientHomeNav.SupportNetwork)
                    }
                }
            )
        }
    ) { padding ->
        PatientHomeScreen(
            state = state,
            onQuickClinicalHistory = { onNavigate(PatientHomeNav.ClinicalHistory) },
            onQuickSupportNetwork = { onNavigate(PatientHomeNav.SupportNetwork) },
            onExploreProfessionals = { onNavigate(PatientHomeNav.ProfessionalsList) },
            onOpenAppointmentsTab = { onNavigate(PatientHomeNav.AppointmentsTab) },
            onOpenMessagesTab = { onNavigate(PatientHomeNav.MessagesTab) },
            onRefresh = { vm.refresh() },
            contentPadding = padding,
            onOpenProfile = { onNavigate(PatientHomeNav.Profile) }
        )

        val pending = reviewUi.pending
        if (reviewUi.showing && pending != null) {
            ReviewPromptDialog(
                professionalName = pending.professionalName,
                saving = reviewUi.saving,
                onSubmit = { stars, comment -> reviewVm.submit(stars, comment) },
                onMissed  = { comment -> reviewVm.markMissed(comment) },
                onDismiss = { reviewVm.dismiss() }
            )
        }

    }
}