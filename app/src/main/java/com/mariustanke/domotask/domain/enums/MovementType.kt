package com.mariustanke.domotask.domain.enums

enum class MovementType(val value: String, val label: String) {
    ADD("add", "Añadir"),
    REMOVE("remove", "Quitar"),
    SET("set", "Ajustar");

    companion object {
        fun fromValue(value: String): MovementType =
            entries.find { it.value == value } ?: ADD
    }

    override fun toString() = label
}
