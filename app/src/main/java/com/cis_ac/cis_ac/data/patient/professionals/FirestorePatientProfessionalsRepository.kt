package com.cis_ac.cis_ac.data.patient.professionals

import com.cis_ac.cis_ac.core.model.Discipline
import com.cis_ac.cis_ac.core.model.Modality
import com.cis_ac.cis_ac.core.model.Population
import com.cis_ac.cis_ac.core.model.idsToModalities
import com.cis_ac.cis_ac.core.model.idsToPopulations
import com.cis_ac.cis_ac.ui.feature.patient.professionals.PatientProfessionalItem
import com.cis_ac.cis_ac.ui.feature.patient.professionals.ProfessionalsFilters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestorePatientProfessionalsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : PatientProfessionalsRepository {

    override suspend fun fetchProfessionals(filters: ProfessionalsFilters): List<PatientProfessionalItem> =
        withContext(Dispatchers.IO) {
            val snap = db.collection("professionals")
                .whereEqualTo("active", true)
                .whereEqualTo("verified", true)
                .get()
                .await()

            val raw = snap.documents.mapNotNull { d ->
                val uid = d.id
                val fullName = d.getString("fullName") ?: return@mapNotNull null

                val discStr = d.getString("discipline") ?: d.getString("mainDiscipline") ?: "PSYCHOLOGY"
                val discipline = runCatching { Discipline.valueOf(discStr) }.getOrElse { Discipline.PSYCHOLOGY }

                val populations: Set<Population> = readPopulations(d.get("populations"), d.get("population"))

                val modalities: Set<Modality> = readModalities(d.get("modalities"), d.get("modality"))

                Raw(
                    uid = uid,
                    fullName = fullName,
                    discipline = discipline,
                    populations = populations,
                    modalities = modalities
                )
            }

            if (raw.isEmpty()) return@withContext emptyList()

            val byDiscipline = filters.discipline?.let { wanted ->
                raw.filter { it.discipline == wanted }
            } ?: raw

            val byPopulation = filters.populationType?.let { uiLabel ->
                val wanted = uiPopulationToEnum(uiLabel)
                if (wanted == null) byDiscipline else byDiscipline.filter { it.populations.contains(wanted) }
            } ?: byDiscipline

            val byModality = filters.modality?.let { uiLabel ->
                val wanted = uiModalityToEnum(uiLabel)
                if (wanted == null) byPopulation else byPopulation.filter { it.modalities.contains(wanted) }
            } ?: byPopulation

            val byQuery = filters.query.trim().lowercase().takeIf { it.isNotEmpty() }?.let { q ->
                byModality.filter { item ->
                    item.fullName.lowercase().contains(q) ||
                            item.discipline.name.lowercase().contains(q)
                }
            } ?: byModality

            if (byQuery.isEmpty()) return@withContext emptyList()

            coroutineScope {
                byQuery.map { r ->
                    async {
                        val reviews = db.collection("reviews")
                            .whereEqualTo("professionalId", r.uid)
                            .whereEqualTo("status", "ATTENDED")
                            .get()
                            .await()

                        val ratings = reviews.documents.mapNotNull { rv ->
                            when (val any = rv.get("rating")) {
                                is Number -> any.toDouble()
                                is String -> any.toDoubleOrNull()
                                else -> null
                            }
                        }
                        val avg = if (ratings.isNotEmpty()) ratings.sum() / ratings.size else null

                        PatientProfessionalItem(
                            uid = r.uid,
                            fullName = r.fullName,
                            discipline = r.discipline,
                            rating = avg
                        )
                    }
                }.awaitAll()
            }
        }


    private data class Raw(
        val uid: String,
        val fullName: String,
        val discipline: Discipline,
        val populations: Set<Population>,
        val modalities: Set<Modality>
    )

    private fun uiPopulationToEnum(label: String): Population? = when (label) {
        "Infantes" -> Population.INFANTES
        "Adolescentes" -> Population.ADOLESCENTES
        "Adultos" -> Population.ADULTOS
        "Adultos mayores" -> Population.ADULTOS_MAYORES
        else -> null
    }

    private fun uiModalityToEnum(label: String): Modality? = when (label) {
        "Presencial" -> Modality.PRESENCIAL
        "En línea"   -> Modality.EN_LINEA
        "Domicilio"  -> Modality.DOMICILIO
        else -> null
    }


    private fun readPopulations(many: Any?, one: Any?): Set<Population> {
        val fromMany = when (many) {
            is List<*> -> {
                val asInts = many.mapNotNull { (it as? Number)?.toInt() }
                if (asInts.isNotEmpty()) idsToPopulations(asInts)
                else {
                    many.mapNotNull { s ->
                        (s as? String)?.let { tryPopFromString(it) }
                    }.toSet()
                }
            }
            else -> emptySet()
        }

        val fromOne = when (one) {
            is Number -> idsToPopulations(listOf(one.toInt()))
            is String -> setOfNotNull(tryPopFromString(one))
            else -> emptySet()
        }

        return (fromMany + fromOne).toSet()
    }

    private fun tryPopFromString(raw: String): Population? {
        runCatching { Population.valueOf(raw) }.getOrNull()?.let { return it }
        return uiPopulationToEnum(raw)
    }

    private fun readModalities(many: Any?, one: Any?): Set<Modality> {
        val fromMany = when (many) {
            is List<*> -> {
                val asInts = many.mapNotNull { (it as? Number)?.toInt() }
                if (asInts.isNotEmpty()) idsToModalities(asInts)
                else {
                    many.mapNotNull { s ->
                        (s as? String)?.let { tryModFromString(it) }
                    }.toSet()
                }
            }
            else -> emptySet()
        }

        val fromOne = when (one) {
            is Number -> idsToModalities(listOf(one.toInt()))
            is String -> setOfNotNull(tryModFromString(one))
            else -> emptySet()
        }

        return (fromMany + fromOne).toSet()
    }

    private fun tryModFromString(raw: String): Modality? {
        runCatching { Modality.valueOf(raw) }.getOrNull()?.let { return it }
        return when (raw) {
            "Presencial" -> Modality.PRESENCIAL
            "En línea", "En linea" -> Modality.EN_LINEA
            "Domicilio" -> Modality.DOMICILIO
            else -> null
        }
    }

    private fun <T> setOfNotNull(value: T?): Set<T> = if (value == null) emptySet() else setOf(value)
}
