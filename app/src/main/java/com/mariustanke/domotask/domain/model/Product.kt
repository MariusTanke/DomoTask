package com.mariustanke.domotask.domain.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String? = null,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
