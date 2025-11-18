package com.cis_ac.cis_ac.ui.feature.patient.professionalprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.Discipline
import com.cis_ac.cis_ac.data.home.FirestoreReviewsRepository
import com.cis_ac.cis_ac.data.home.ReviewsRepository
import com.cis_ac.cis_ac.data.professional.FirestoreProfessionalProfileRepository
import com.cis_ac.cis_ac.data.professional.ProfessionalProfileRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfessionalProfileViewModel(
    private val repo: ProfessionalProfileRepository = FirestoreProfessionalProfileRepository(),
    private val reviewsRepo: ReviewsRepository = FirestoreReviewsRepository()

) : ViewModel() {

    private val _ui = MutableStateFlow<ProfessionalProfileUiState>(ProfessionalProfileUiState.Loading)
    val ui: StateFlow<ProfessionalProfileUiState> = _ui

    fun load(uid: String) {
        _ui.value = ProfessionalProfileUiState.Loading
        viewModelScope.launch {
            when (val res = repo.getProfessional(uid)) {
                is Result.Success -> {
                    val pro = res.data

                    val approachForUi: String? =
                        if (pro.mainDiscipline == Discipline.PSYCHOLOGY) pro.approach else null

                    val topicsList: List<String> =
                        pro.topics
                            .split(',')
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                    val modalitiesList: List<String> = pro.modalities.map { it.toString() }.sorted()
                    val sessionsList: List<String> = pro.sessionTypes.map { it.toString() }.sorted()

                    val summaryForUi: String = pro.semblance.ifBlank { "" }

                    val avgDef  = async { reviewsRepo.getAverageForProfessional(pro.uid) }
                    val testiDef = async { reviewsRepo.getRecentTestimonials(pro.uid, limit = 10) }

                    val rating = avgDef.await()
                    val testimonials = testiDef.await()

                    _ui.value = ProfessionalProfileUiState.Content(
                        uid = pro.uid,
                        fullName = pro.fullName,
                        discipline = pro.mainDiscipline,
                        licenseNumber = pro.licenseNumber.ifBlank { null },
                        expertiz = pro.expertiz,
                        approach = approachForUi,
                        topics = topicsList,
                        sessionTypes = sessionsList,
                        modalities = modalitiesList,
                        summary = summaryForUi,
                        rating = rating,
                        testimonials = testimonials
                    )
                }
                is Result.Error -> _ui.value = ProfessionalProfileUiState.Error(res.message)
                else -> Unit
            }
        }
    }
}
