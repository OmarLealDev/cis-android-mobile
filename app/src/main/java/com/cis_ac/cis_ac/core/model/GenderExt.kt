package com.cis_ac.cis_ac.core.model

fun Gender.label(): String = when (this) {
    Gender.Male      -> "Masculino"
    Gender.Female       -> "Femenino"
    Gender.Other   -> "Otro"
    Gender.Unspecified -> "No especificado"
}

