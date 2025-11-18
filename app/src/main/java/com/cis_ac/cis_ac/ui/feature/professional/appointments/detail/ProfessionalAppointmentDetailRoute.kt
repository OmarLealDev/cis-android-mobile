@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.cis_ac.cis_ac.ui.feature.professional.appointments.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.data.appointments.AppointmentRepository
import com.cis_ac.cis_ac.data.appointments.FirestoreAppointmentRepository
import com.cis_ac.cis_ac.data.userprofile.dto.PatientFS
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ProfessionalAppointmentDetailRoute(
    appointmentId: String,
    vm: ProfessionalAppointmentDetailViewModel = viewModel(),
    onBack: () -> Unit,
    onUpdated: (confirmed: Boolean, message: String) -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    LaunchedEffect(appointmentId) { vm.load(appointmentId) }

    val title = when (val s = ui) {
        is DetailUi.Content -> if (s.confirmed) "Detalle de cita" else "Confirmar cita"
        else -> "Detalle de cita"
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 2.dp) {
                Column {
                    CenterAlignedTopAppBar(
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                        title = { Text(title) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
        }
    ) { padding ->
        when (val s = ui) {
            DetailUi.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is DetailUi.Error -> Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                ElevatedButton(onClick = { vm.load(appointmentId) }) { Text("Reintentar") }
            }

            is DetailUi.Content -> DetailContent(
                data = s,
                onConfirm = {
                    vm.confirm(true) {
                        onUpdated(true, "¡Cita confirmada!")
                    }
                },
                onReject  = {
                    vm.cancel {
                        onUpdated(false, "Cita rechazada")
                    }
                },
                contentPadding = padding
            )
        }
    }
}

@Composable
private fun DetailContent(
    data: DetailUi.Content,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    contentPadding: PaddingValues
) {
    val date = LocalDate.ofEpochDay(data.dateEpochDay)
    val dateText = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    val timeText = String.format("%02d:00", data.hour24)

    Column(
        Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.size(96.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Paciente", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                data.patientName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Labeled("Fecha:", dateText)
        Labeled("Hora:", timeText)
        Labeled("Motivo de consulta:", if (data.notes.isBlank()) "El paciente no proporcionó motivo" else data.notes)

        Spacer(Modifier.weight(1f))

        if (!data.confirmed) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                ) { Text("Rechazar") }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                ) { Text("Confirmar") }
            }
        }
    }
}

@Composable
private fun Labeled(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

/* ----------------- UI STATE ----------------- */

sealed interface DetailUi {
    data object Loading : DetailUi
    data class Error(val message: String) : DetailUi
    data class Content(
        val id: String,
        val patientName: String,
        val dateEpochDay: Long,
        val hour24: Int,
        val notes: String,
        val confirmed: Boolean
    ) : DetailUi
}

/* ----------------- VIEWMODEL ----------------- */




/* ----------------- VIEWMODEL ----------------- */

class ProfessionalAppointmentDetailViewModel(
    private val repo: AppointmentRepository = FirestoreAppointmentRepository(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _ui = MutableStateFlow<DetailUi>(DetailUi.Loading)
    val ui: StateFlow<DetailUi> = _ui

    private var currentId: String? = null

    fun load(appointmentId: String) = viewModelScope.launch {
        currentId = appointmentId
        _ui.value = DetailUi.Loading

        when (val res = repo.getById(appointmentId)) {
            is Result.Success -> {
                val a = res.data

                val displayName = a.patientId?.let { pId ->
                    try {
                        val snap = db.collection("patients").document(pId).get().await()
                        val dto = snap.toObject<PatientFS>()
                        dto?.fullName?.takeIf { it.isNotBlank() }
                    } catch (_: Exception) { null }
                } ?: "Paciente"

                _ui.value = DetailUi.Content(
                    id = a.id,
                    patientName = displayName,
                    dateEpochDay = a.dateEpochDay,
                    hour24 = a.hour24,
                    notes = a.notes,
                    confirmed = a.confirmed
                )
            }
            is Result.Error -> _ui.value = DetailUi.Error(res.message)
            else -> Unit
        }
    }

    fun confirm(ok: Boolean, onDone: () -> Unit) = viewModelScope.launch {
        val id = currentId ?: return@launch
        when (repo.confirm(id, ok)) {
            is Result.Success -> onDone()
            else -> Unit
        }
    }

    fun cancel(onDone: () -> Unit) = viewModelScope.launch {
        val id = currentId ?: return@launch
        when (repo.cancel(id, reason = null)) {
            is Result.Success -> onDone()
            else -> Unit
        }
    }
}