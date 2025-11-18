package com.cis_ac.cis_ac.core.model

fun Discipline.label(): String = when (this) {
    Discipline.PSYCHOLOGY      -> "Psicólogo"
    Discipline.NUTRITION       -> "Nutriólogo"
    Discipline.PHYSIOTHERAPY   -> "Fisioterapia"
}