package com.mariustanke.domotask.domain.usecase.inventory

import com.mariustanke.domotask.domain.repository.InventoryRepository
import javax.inject.Inject

class DeleteInventoryItemUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(boardId: String, itemId: String) {
        repository.deleteInventoryItem(boardId, itemId)
    }
}
