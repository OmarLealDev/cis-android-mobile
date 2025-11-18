package com.cis_ac.cis_ac.ui.feature.patient.history

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PatientHistoryRoute(
    vm: PatientHistoryViewModel = viewModel(),
    onBack: () -> Unit,
    onSave: () -> Unit,
    onOpenSection: (Section) -> Unit
) {
    val state = vm.uiState.collectAsStateWithLifecycle().value

    val snackbar = remember { SnackbarHostState() }
    val msg by vm.message.collectAsStateWithLifecycle(initialValue = null)
    LaunchedEffect(msg) { msg?.let { snackbar.showSnackbar(it) } }


    PatientHistoryScreen(
        state = state,
        onBack = onBack,
        onToggleAllergies = vm::toggleAllergies,
        onEditAllergy = { },
        onAddNote = {  },
        onOpenSection = onOpenSection,
        onSave = { vm.save(); onSave() },

        onToggleDiagnoses  = vm::toggleDiagnoses,
        onToggleTreatments = vm::toggleTreatments,
        onToggleMeds       = vm::toggleMeds,
        onAddDiagnoseNote  = vm::addNoteToDiagnoses,
        onAddTreatmentNote = vm::addNoteToTreatments,
        onAddMedNote       = vm::addNoteToMeds,
        onAddAllergyNote   = vm::addNoteToAllergies,

        onToggleGeneral = vm::toggleGeneral,
        onGeneralChange = vm::onGeneralChange,
        onRequestEditNote = vm::onRequestEditNote,
        onCommitNote = vm::onCommitNote,
        onDismissEditor = vm::dismissEditor,
        snackbarHostState = snackbar

    )
}
