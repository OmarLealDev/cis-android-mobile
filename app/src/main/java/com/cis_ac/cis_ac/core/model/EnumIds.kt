package com.cis_ac.cis_ac.core.model



fun modalityId(m: Modality): Int = when (m) {
    Modality.PRESENCIAL -> 1
    Modality.EN_LINEA   -> 2
    Modality.DOMICILIO  -> 3
}

fun sessionsId(s: Sessions): Int = when (s) {
    Sessions.INDIVIDUAL -> 1
    Sessions.PAREJA     -> 2
    Sessions.FAMILIAR   -> 3
    Sessions.EQUIPOS    -> 4
}

fun populationId(p: Population): Int = when (p) {
    Population.INFANTES        -> 1
    Population.ADOLESCENTES    -> 2
    Population.ADULTOS         -> 3
    Population.ADULTOS_MAYORES -> 4
}

/* ===== ID -> Enum ===== */

fun modalityFromId(id: Int): Modality = when (id) {
    1 -> Modality.PRESENCIAL
    2 -> Modality.EN_LINEA
    3 -> Modality.DOMICILIO
    else -> Modality.PRESENCIAL
}

fun sessionsFromId(id: Int): Sessions = when (id) {
    1 -> Sessions.INDIVIDUAL
    2 -> Sessions.PAREJA
    3 -> Sessions.FAMILIAR
    4 -> Sessions.EQUIPOS
    else -> Sessions.INDIVIDUAL
}

fun populationFromId(id: Int): Population = when (id) {
    1 -> Population.INFANTES
    2 -> Population.ADOLESCENTES
    3 -> Population.ADULTOS
    4 -> Population.ADULTOS_MAYORES
    else -> Population.ADULTOS
}


fun modalitiesToIdList(set: Set<Modality>): List<Int>   = set.map(::modalityId)
fun sessionsToIdList(set: Set<Sessions>): List<Int>     = set.map(::sessionsId)
fun populationsToIdList(set: Set<Population>): List<Int> = set.map(::populationId)

fun idsToModalities(ids: List<Int>): Set<Modality>   = ids.map(::modalityFromId).toSet()
fun idsToSessions(ids: List<Int>): Set<Sessions>     = ids.map(::sessionsFromId).toSet()
fun idsToPopulations(ids: List<Int>): Set<Population> = ids.map(::populationFromId).toSet()
