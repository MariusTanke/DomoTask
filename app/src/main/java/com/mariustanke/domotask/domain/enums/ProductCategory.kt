package com.mariustanke.domotask.domain.enums

enum class ProductCategory(val value: String, val label: String) {
    FRUIT("fruit", "Fruta"),
    VEGETABLE("vegetable", "Verdura"),
    MEAT("meat", "Carne"),
    FISH("fish", "Pescado"),
    DAIRY("dairy", "Lácteos"),
    FROZEN("frozen", "Congelados"),
    BEVERAGE("beverage", "Bebida"),
    BAKERY("bakery", "Panadería"),
    CLEANING("cleaning", "Limpieza"),
    HYGIENE("hygiene", "Higiene"),
    PANTRY("pantry", "Despensa"),
    SNACK("snack", "Snacks"),
    INTERNATIONAL("international", "Internacional"),
    OTHER("other", "Otro");

    companion object {
        fun fromValue(value: String): ProductCategory =
            entries.find { it.value == value } ?: OTHER
    }

    override fun toString() = label
}
