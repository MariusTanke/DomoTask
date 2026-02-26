package com.mariustanke.domotask.domain.enums

enum class ProductUnit(val value: String, val label: String, val abbreviation: String) {
    UNIT("unit", "Unidad", "ud"),
    KILOGRAM("kg", "Kilogramo", "kg"),
    GRAM("g", "Gramo", "g"),
    LITER("l", "Litro", "L"),
    MILLILITER("ml", "Mililitro", "mL"),
    PACK("pack", "Pack", "pack"),
    DOZEN("dozen", "Docena", "doc"),
    BOTTLE("bottle", "Botella", "bot");

    companion object {
        fun fromValue(value: String): ProductUnit =
            entries.find { it.value == value } ?: UNIT
    }

    override fun toString() = label
}
