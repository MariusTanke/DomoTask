package com.mariustanke.domotask.domain.usecase.inventory

import com.mariustanke.domotask.domain.model.InventoryHistory
import com.mariustanke.domotask.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetItemHistoryUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    operator fun invoke(boardId: String, itemId: String): Flow<List<InventoryHistory>> =
        repository.getItemHistory(boardId, itemId)
}
