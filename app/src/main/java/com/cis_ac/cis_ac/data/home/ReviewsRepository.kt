package com.cis_ac.cis_ac.data.home

import com.cis_ac.cis_ac.core.model.AppointmentReview
import com.cis_ac.cis_ac.core.model.PendingReview
import com.cis_ac.cis_ac.ui.feature.patient.professionalprofile.Testimonial

data class ReviewAggregate(val average: Double, val count: Int)

interface ReviewsRepository {
    suspend fun findOldestPendingReviewForPatient(patientId: String, nowMillis: Long): PendingReview?

    suspend fun submitAttended(review: AppointmentReview)

    suspend fun markMissed(appointmentId: String, professionalId: String, patientId: String, comment: String?)
    suspend fun getAverageForProfessional(professionalId: String): Double?
    suspend fun getRecentTestimonials(professionalId: String, limit: Int = 10): List<Testimonial>


}