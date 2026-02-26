package com.mariustanke.domotask.domain.usecase.inventory

data class InventoryUseCases(
    val getInventoryItems: GetInventoryItemsUseCase,
    val addInventoryItem: AddInventoryItemUseCase,
    val saveTransaction: SaveInventoryTransactionUseCase,
    val deleteInventoryItem: DeleteInventoryItemUseCase,
    val getInventoryHistory: GetInventoryHistoryUseCase,
    val getItemHistory: GetItemHistoryUseCase,
    val searchProducts: SearchProductsUseCase,
    val getProducts: GetProductsUseCase
)
