package com.cis_ac.cis_ac.data.home

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.AppointmentReview
import com.cis_ac.cis_ac.core.model.PendingReview
import com.cis_ac.cis_ac.core.model.ReviewStatus
import com.cis_ac.cis_ac.data.appointments.AppointmentRepository
import com.cis_ac.cis_ac.data.appointments.FirestoreAppointmentRepository
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import com.cis_ac.cis_ac.ui.feature.patient.professionalprofile.Testimonial
import java.time.Instant
import kotlin.math.max


class FirestoreReviewsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val apptRepo: AppointmentRepository = FirestoreAppointmentRepository(),
    private val tz: ZoneId = runCatching { ZoneId.of("America/Mazatlan") }.getOrElse { ZoneId.systemDefault() }
) : ReviewsRepository {
    private val aggCol get() = db.collection("review_aggregates")


    override suspend fun findOldestPendingReviewForPatient(
        patientId: String,
        nowMillis: Long
    ): PendingReview? = withContext(Dispatchers.IO) {
        when (val res = apptRepo.listPatientAppointments(patientId)) {
            !is Result.Success -> return@withContext null
            else -> {
                val all = res.data.orEmpty().filter { it.active != false }
                if (all.isEmpty()) return@withContext null

                val now = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(nowMillis), tz)
                val todayEpochDay = now.toLocalDate().toEpochDay().toInt()
                val nowHour = now.hour

                val past = all.filter { a ->
                    when {
                        a.dateEpochDay < todayEpochDay -> true
                        a.dateEpochDay > todayEpochDay -> false
                        else -> a.hour24 < nowHour
                    }
                }.sortedWith(compareBy({ it.dateEpochDay }, { it.hour24 }))

                if (past.isEmpty()) return@withContext null

                val firstWithoutReview = past.firstOrNull { a ->
                    val apptId = a.id ?: "${a.patientId}_${a.dateEpochDay}_${a.hour24}"
                    val snap = db.collection("reviews")
                        .whereEqualTo("appointmentId", apptId)
                        .limit(1)
                        .get()
                        .await()
                    snap.isEmpty
                } ?: return@withContext null

                val profId = firstWithoutReview.professionalId ?: return@withContext null
                val apptId = firstWithoutReview.id ?: "${firstWithoutReview.patientId}_${firstWithoutReview.dateEpochDay}_${firstWithoutReview.hour24}"

                val date = LocalDate.ofEpochDay(firstWithoutReview.dateEpochDay.toLong())
                val ldt  = LocalDateTime.of(date, LocalTime.of(firstWithoutReview.hour24, 0))
                val whenMs = ldt.atZone(tz).toInstant().toEpochMilli()

                val profName = runCatching {
                    db.collection("professionals").document(profId).get().await()
                        .getString("fullName").orEmpty().ifBlank { "Profesional" }
                }.getOrDefault("Profesional")

                PendingReview(
                    appointmentId    = apptId,
                    professionalId   = profId,
                    dateTimeMillis   = whenMs,
                    professionalName = profName
                )
            }
        }
    }
    override suspend fun getRecentTestimonials(professionalId: String, limit: Int): List<Testimonial> =
        withContext(Dispatchers.IO) {
            val snap = db.collection("reviews")
                .whereEqualTo("professionalId", professionalId)
                .whereEqualTo("status", ReviewStatus.ATTENDED.name)
                .get()
                .await()

            val now = LocalDate.now(tz)

            snap.documents
                .mapNotNull { d ->
                    val comment = d.getString("comment")?.trim().orEmpty()
                    if (comment.isEmpty()) return@mapNotNull null

                    val rating = when (val v = d.get("rating")) {
                        is Number -> v.toInt()
                        is String -> v.toIntOrNull()
                        else -> null
                    } ?: return@mapNotNull null

                    val createdAtMs = d.getLong("createdAt") ?: System.currentTimeMillis()
                    val createdDate = Instant.ofEpochMilli(createdAtMs).atZone(tz).toLocalDate()
                    val months = max(0, (now.year - createdDate.year) * 12 + (now.monthValue - createdDate.monthValue))

                    val author = d.getString("name") ?: "Paciente"

                    Testimonial(
                        author = author,
                        monthsAgo = months,
                        text = comment,
                        stars = rating.coerceIn(1, 5)
                    )
                }
                .sortedBy { it.monthsAgo }
                .take(limit)
        }

    override suspend fun getAverageForProfessional(professionalId: String): Double? =
        withContext(Dispatchers.IO) {
            val snap = db.collection("reviews")
                .whereEqualTo("professionalId", professionalId)
                .whereEqualTo("status", ReviewStatus.ATTENDED.name)
                .get()
                .await()

            val ratings = snap.documents.mapNotNull { d ->
                when (val v = d.get("rating")) {
                    is Number -> v.toDouble()
                    is String -> v.toDoubleOrNull()
                    else -> null
                }
            }
            if (ratings.isEmpty()) null else ratings.sum() / ratings.size
        }


    override suspend fun submitAttended(review: AppointmentReview): Unit = withContext(Dispatchers.IO) {


        val data = hashMapOf(
            "appointmentId" to review.appointmentId,
            "professionalId" to review.professionalId,
            "status"         to ReviewStatus.ATTENDED.name,
            "rating"         to (review.rating ?: 0),
            "comment"        to (review.comment ?: ""),
            "createdAt"      to System.currentTimeMillis()
        )
        db.collection("reviews").add(data).await()

        runCatching {
            db.collection("appointments").document(review.appointmentId)
                .update(
                    mapOf(
                        "reviewStatus" to ReviewStatus.ATTENDED.name,
                        "reviewedAt"   to System.currentTimeMillis()
                    )
                )
                .await()
        }

        Unit
    }

    override suspend fun markMissed(
        appointmentId: String,
        professionalId: String,
        patientId: String,
        comment: String?
    ): Unit = withContext(Dispatchers.IO) {
        val data = hashMapOf(
            "appointmentId" to appointmentId,
            "professionalId" to professionalId,
            "patientId"      to patientId,
            "status"         to ReviewStatus.MISSED.name,
            "rating"         to 0,
            "comment"        to (comment ?: ""),
            "createdAt"      to System.currentTimeMillis()
        )
        db.collection("reviews").add(data).await()

        runCatching {
            db.collection("appointments").document(appointmentId)
                .update(
                    mapOf(
                        "reviewStatus" to ReviewStatus.MISSED.name,
                        "reviewedAt"   to System.currentTimeMillis()
                    )
                )
                .await()
        }
        Unit
    }
}
