package com.cis_ac.cis_ac.ui.feature.patient.history

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@Composable
fun PatientHistoryRouteForPro(
    patientUid: String,
    onBack: () -> Unit
) {
    val vm: PatientHistoryViewModel = viewModel(factory = PatientHistoryVMFactory(patientUid))
    val state = vm.uiState.collectAsStateWithLifecycle().value

    val snackbar = remember { SnackbarHostState() }
    val msg by vm.message.collectAsStateWithLifecycle(initialValue = null)
    LaunchedEffect(msg) { msg?.let { snackbar.showSnackbar(it) } }

    PatientHistoryScreen(
        state = state,
        onBack = onBack,

        onEditAllergy = {  },
        onAddNote = { },
        onOpenSection = {  },

        onToggleAllergies = vm::toggleAllergies,
        onToggleDiagnoses  = vm::toggleDiagnoses,
        onToggleTreatments = vm::toggleTreatments,
        onToggleMeds       = vm::toggleMeds,
        onToggleGeneral    = vm::toggleGeneral,

        onGeneralChange = { },

        onAddDiagnoseNote  = vm::addNoteToDiagnoses,
        onAddTreatmentNote = vm::addNoteToTreatments,
        onAddMedNote       = vm::addNoteToMeds,
        onAddAllergyNote   = vm::addNoteToAllergies,
        onRequestEditNote  = vm::onRequestEditNote,
        onCommitNote       = vm::onCommitNote,
        onDismissEditor    = vm::dismissEditor,

        onSave = {  },

        snackbarHostState = snackbar,

        generalEditable = false
    )
}
