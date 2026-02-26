package com.mariustanke.domotask.domain.model

data class InventoryItem(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val productCategory: String = "",
    val quantity: Double = 0.0,
    val unit: String = "unit",
    val minQuantity: Double = 0.0,
    val lastUpdatedBy: String = "",
    val lastUpdatedAt: Long = System.currentTimeMillis()
)
