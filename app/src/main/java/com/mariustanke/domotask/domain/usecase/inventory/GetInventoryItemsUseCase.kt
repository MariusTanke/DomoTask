package com.mariustanke.domotask.domain.usecase.inventory

import com.mariustanke.domotask.domain.model.InventoryItem
import com.mariustanke.domotask.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInventoryItemsUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    operator fun invoke(boardId: String): Flow<List<InventoryItem>> =
        repository.getInventoryItems(boardId)
}
