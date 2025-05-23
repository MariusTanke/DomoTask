package com.mariustanke.domotask.domain.enums

enum class UrgencyEnum(val value: Int, val label: String) {
    VERY_LOW(1, "Very Low"),
    LOW     (2, "Low"),
    NORMAL  (3, "Normal"),
    HIGH    (4, "High"),
    VERY_HIGH(5, "Very High");

    companion object {
        fun fromValue(value: Int): UrgencyEnum =
            entries.find { it.value == value } ?: NORMAL
    }

    override fun toString() = label
}



