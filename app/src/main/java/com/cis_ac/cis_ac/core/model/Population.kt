package com.cis_ac.cis_ac.core.model

enum class Population(val value: Int, val spanishName: String) {
    INFANTES(1, "Infantes"),
    ADOLESCENTES(2, "Adolescentes"),
    ADULTOS(3, "Adultos"),
    ADULTOS_MAYORES(4, "Adultos mayores");

    companion object {
        fun fromInt(v: Int): Population = values().find { it.value == v } ?: ADULTOS
        fun fromAny(obj: Any?): Population = when (obj) {
            is Population -> obj
            is Number -> fromInt(obj.toInt())
            is String -> fromInt(obj.toIntOrNull() ?: ADULTOS.value)
            else -> ADULTOS
        }
        val entries: List<Population> get() = values().toList()
    }

    override fun toString(): String = spanishName
}
