package com.cis_ac.cis_ac.ui.feature.home.patient.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.model.PendingReview
import com.cis_ac.cis_ac.core.model.AppointmentReview
import com.cis_ac.cis_ac.core.model.ReviewStatus
import com.cis_ac.cis_ac.data.home.FirestoreReviewsRepository
import com.cis_ac.cis_ac.data.home.ReviewsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.ZoneId

data class ReviewPromptUiState(
    val loading: Boolean = false,
    val pending: PendingReview? = null,
    val showing: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null
)
sealed interface ReviewEvent {
    data class Toast(val message: String) : ReviewEvent
}
class PatientReviewPromptViewModel(
    private val repo: ReviewsRepository = FirestoreReviewsRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val tz: ZoneId = runCatching { ZoneId.of("America/Mazatlan") }.getOrElse { ZoneId.systemDefault() }
) : ViewModel() {

    private val _ui = MutableStateFlow(ReviewPromptUiState(loading = true))
    val ui: StateFlow<ReviewPromptUiState> = _ui

    private val _events = MutableSharedFlow<ReviewEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ReviewEvent> = _events.asSharedFlow()


    fun load() = viewModelScope.launch {
        _ui.value = _ui.value.copy(loading = true, error = null)
        val patientId = auth.currentUser?.uid ?: run {
            _ui.value = ReviewPromptUiState(loading = false, pending = null, showing = false)
            return@launch
        }
        val nowMs = System.currentTimeMillis()
        val p = repo.findOldestPendingReviewForPatient(patientId, nowMs)
        _ui.value = ReviewPromptUiState(loading = false, pending = p, showing = (p != null))
    }

    fun dismiss() { _ui.value = _ui.value.copy(showing = false) }

    fun submit(rating: Int, comment: String) = viewModelScope.launch {
        val p = _ui.value.pending ?: return@launch
        val patientId = auth.currentUser?.uid ?: return@launch
        _ui.value = _ui.value.copy(saving = true, error = null)
        try {
            repo.submitAttended(
                AppointmentReview(
                    appointmentId = p.appointmentId,
                    professionalId = p.professionalId,
                    patientId = patientId,
                    status = ReviewStatus.ATTENDED,
                    rating = rating,
                    comment = comment
                )
            )

            _ui.value = ReviewPromptUiState(loading = false, pending = null, showing = false)
            _events.tryEmit(ReviewEvent.Toast("¡Gracias por tu opinión!"))

        } catch (t: Throwable) {
            _ui.value = _ui.value.copy(saving = false, error = t.message ?: "Error al enviar")
        }
    }

    fun markMissed(comment: String?) = viewModelScope.launch {
        val p = _ui.value.pending ?: return@launch
        val patientId = auth.currentUser?.uid ?: return@launch
        _ui.value = _ui.value.copy(saving = true, error = null)
        try {
            repo.markMissed(p.appointmentId, p.professionalId, patientId, comment)
            _ui.value = ReviewPromptUiState(loading = false, pending = null, showing = false)
            _events.tryEmit(ReviewEvent.Toast("¡Gracias por avisarnos!"))

        } catch (t: Throwable) {
            _ui.value = _ui.value.copy(saving = false, error = t.message ?: "Error al reportar")
        }
    }
}
