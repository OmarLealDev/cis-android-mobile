package com.cis_ac.cis_ac.core.model

enum class Modality(val value: Int, val spanishName: String) {
    PRESENCIAL(1, "Presencial"),
    EN_LINEA(2, "En línea"),
    DOMICILIO(3, "Domicilio");

    companion object {
        fun fromInt(v: Int): Modality = values().find { it.value == v } ?: PRESENCIAL
        fun fromAny(obj: Any?): Modality = when (obj) {
            is Modality -> obj
            is Number -> fromInt(obj.toInt())
            is String -> fromInt(obj.toIntOrNull() ?: PRESENCIAL.value)
            else -> PRESENCIAL
        }
        val entries: List<Modality> get() = values().toList()
    }

    override fun toString(): String = spanishName
}
