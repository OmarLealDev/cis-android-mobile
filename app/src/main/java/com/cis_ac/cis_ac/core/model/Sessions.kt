package com.cis_ac.cis_ac.core.model

enum class Sessions(val value: Int, val spanishName: String) {
    INDIVIDUAL(1, "Individual"),
    PAREJA(2, "Pareja"),
    FAMILIAR(3, "Familiar"),
    EQUIPOS(4, "Equipos");

    companion object {
        fun fromInt(v: Int): Sessions = values().find { it.value == v } ?: INDIVIDUAL
        fun fromAny(obj: Any?): Sessions = when (obj) {
            is Sessions -> obj
            is Number -> fromInt(obj.toInt())
            is String -> fromInt(obj.toIntOrNull() ?: INDIVIDUAL.value)
            else -> INDIVIDUAL
        }
        val entries: List<Sessions> get() = values().toList()
    }

    override fun toString(): String = spanishName
}
