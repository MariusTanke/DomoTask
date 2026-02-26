package com.mariustanke.domotask.domain.usecase.inventory

import com.mariustanke.domotask.domain.model.InventoryItem
import com.mariustanke.domotask.domain.model.Product
import com.mariustanke.domotask.domain.repository.InventoryRepository
import javax.inject.Inject

class AddInventoryItemUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(
        boardId: String,
        product: Product,
        initialQuantity: Double,
        unit: String,
        userId: String,
        userName: String
    ): String {
        val item = InventoryItem(
            productId = product.id,
            productName = product.name,
            productCategory = product.category,
            quantity = initialQuantity,
            unit = unit,
            lastUpdatedBy = userId,
            lastUpdatedAt = System.currentTimeMillis()
        )
        return repository.addInventoryItem(boardId, item)
    }
}
