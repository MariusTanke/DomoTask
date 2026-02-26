package com.mariustanke.domotask.domain.model

data class HistoryItemChange(
    val inventoryItemId: String = "",
    val productName: String = "",
    val movementType: String = "add",
    val unit: String = "unit",
    val previousQuantity: Double = 0.0,
    val modifiedQuantity: Double = 0.0,
    val resultingQuantity: Double = 0.0
)

data class InventoryHistory(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val items: List<HistoryItemChange> = emptyList()
)
