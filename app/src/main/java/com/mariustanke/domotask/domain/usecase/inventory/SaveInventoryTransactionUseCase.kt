package com.mariustanke.domotask.domain.usecase.inventory

import com.mariustanke.domotask.domain.model.InventoryHistory
import com.mariustanke.domotask.domain.model.InventoryItem
import com.mariustanke.domotask.domain.repository.InventoryRepository
import javax.inject.Inject

class SaveInventoryTransactionUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(
        boardId: String,
        updates: List<InventoryItem>,
        newItems: List<InventoryItem>,
        deletions: List<String>,
        history: InventoryHistory
    ) {
        repository.saveTransaction(boardId, updates, newItems, deletions, history)
    }
}
