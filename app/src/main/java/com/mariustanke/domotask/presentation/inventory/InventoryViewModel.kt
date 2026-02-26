package com.mariustanke.domotask.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mariustanke.domotask.domain.model.HistoryItemChange
import com.mariustanke.domotask.domain.model.InventoryHistory
import com.mariustanke.domotask.domain.model.InventoryItem
import com.mariustanke.domotask.domain.model.Product
import com.mariustanke.domotask.domain.usecase.auth.UserUseCases
import com.mariustanke.domotask.domain.usecase.inventory.InventoryUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PendingChange(
    val item: InventoryItem,
    val originalQuantity: Double,
    val newQuantity: Double,
    val isNew: Boolean,
    val isDeleted: Boolean = false
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryUseCases: InventoryUseCases,
    private val userUseCases: UserUseCases,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _firestoreItems = MutableStateFlow<List<InventoryItem>>(emptyList())

    private val _pendingChanges = MutableStateFlow<Map<String, PendingChange>>(emptyMap())
    val pendingChanges: StateFlow<Map<String, PendingChange>> = _pendingChanges.asStateFlow()

    private val _displayItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val displayItems: StateFlow<List<InventoryItem>> = _displayItems.asStateFlow()

    private val _hasPendingChanges = MutableStateFlow(false)
    val hasPendingChanges: StateFlow<Boolean> = _hasPendingChanges.asStateFlow()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    private val _history = MutableStateFlow<List<InventoryHistory>>(emptyList())
    val history: StateFlow<List<InventoryHistory>> = _history.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    val searchResults: StateFlow<List<Product>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    private var currentBoardId: String? = null
    private var itemsJob: Job? = null
    private var historyJob: Job? = null
    private var newItemCounter = 0

    init {
        viewModelScope.launch {
            combine(_firestoreItems, _pendingChanges) { fsItems, pending ->
                mergeDisplayItems(fsItems, pending)
            }.collect { merged ->
                _displayItems.value = merged
            }
        }
        viewModelScope.launch {
            _pendingChanges.collect { pending ->
                _hasPendingChanges.value = pending.isNotEmpty()
                _pendingCount.value = pending.size
            }
        }
    }

    fun loadInventory(boardId: String) {
        if (currentBoardId == boardId && itemsJob?.isActive == true) return
        currentBoardId = boardId

        itemsJob?.cancel()
        historyJob?.cancel()

        itemsJob = viewModelScope.launch {
            inventoryUseCases.getInventoryItems(boardId).collect { _firestoreItems.value = it }
        }
        historyJob = viewModelScope.launch {
            inventoryUseCases.getInventoryHistory(boardId).collect { _history.value = it }
        }
        viewModelScope.launch {
            inventoryUseCases.getProducts().collect { _products.value = it }
        }
    }

    fun searchProducts(query: String) {
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                _searchResults.value = inventoryUseCases.searchProducts(query)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }

    /**
     * Update quantity locally by delta (+1 or -1). Does NOT touch Firestore.
     */
    fun updateLocalQuantity(itemId: String, delta: Double) {
        val pending = _pendingChanges.value.toMutableMap()
        val existing = pending[itemId]

        // Don't allow quantity changes on deleted items
        if (existing?.isDeleted == true) return

        if (existing != null) {
            val updated = (existing.newQuantity + delta).coerceAtLeast(0.0)
            pending[itemId] = existing.copy(newQuantity = updated)
        } else {
            // Find in Firestore items
            val fsItem = _firestoreItems.value.find { it.id == itemId } ?: return
            val updated = (fsItem.quantity + delta).coerceAtLeast(0.0)
            if (updated != fsItem.quantity) {
                pending[itemId] = PendingChange(
                    item = fsItem,
                    originalQuantity = fsItem.quantity,
                    newQuantity = updated,
                    isNew = false
                )
            }
        }

        // Remove if back to original (for non-new items)
        val change = pending[itemId]
        if (change != null && !change.isNew && change.newQuantity == change.originalQuantity) {
            pending.remove(itemId)
        }

        _pendingChanges.value = pending
    }

    /**
     * Add a new product locally. Does NOT touch Firestore until saveAll.
     */
    fun addNewProduct(product: Product, quantity: Double, unit: String) {
        val user = currentUser ?: return

        // Check if already in Firestore items
        val existingFs = _firestoreItems.value.find { it.productId == product.id }
        if (existingFs != null) {
            _error.value = "Este producto ya está en el inventario"
            return
        }

        // Check if already in pending new items
        val existingPending = _pendingChanges.value.values.find { it.item.productId == product.id }
        if (existingPending != null) {
            _error.value = "Este producto ya está pendiente de añadir"
            return
        }

        newItemCounter++
        val tempId = "new_${System.currentTimeMillis()}_$newItemCounter"
        val newItem = InventoryItem(
            id = tempId,
            productId = product.id,
            productName = product.name,
            productCategory = product.category,
            quantity = quantity,
            unit = unit,
            lastUpdatedBy = user.uid,
            lastUpdatedAt = System.currentTimeMillis()
        )

        val pending = _pendingChanges.value.toMutableMap()
        pending[tempId] = PendingChange(
            item = newItem,
            originalQuantity = 0.0,
            newQuantity = quantity,
            isNew = true
        )
        _pendingChanges.value = pending
    }

    /**
     * Save all pending changes as a single atomic transaction to Firestore.
     */
    fun saveAll(comment: String = "") {
        val boardId = currentBoardId ?: return
        val user = currentUser ?: return
        val pending = _pendingChanges.value
        if (pending.isEmpty()) return

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val userName = userUseCases.getUser(user.uid)?.name ?: user.displayName.orEmpty()

                val updates = mutableListOf<InventoryItem>()
                val newItems = mutableListOf<InventoryItem>()
                val deletions = mutableListOf<String>()
                val historyItems = mutableListOf<HistoryItemChange>()

                for ((_, change) in pending) {
                    val movementType = when {
                        change.isDeleted -> "delete"
                        change.isNew -> "add"
                        change.newQuantity > change.originalQuantity -> "add"
                        change.newQuantity < change.originalQuantity -> "remove"
                        else -> "set"
                    }

                    historyItems.add(
                        HistoryItemChange(
                            inventoryItemId = if (change.isNew) "" else change.item.id,
                            productName = change.item.productName,
                            movementType = movementType,
                            unit = change.item.unit,
                            previousQuantity = change.originalQuantity,
                            modifiedQuantity = kotlin.math.abs(change.newQuantity - change.originalQuantity),
                            resultingQuantity = change.newQuantity
                        )
                    )

                    when {
                        change.isDeleted -> {
                            deletions.add(change.item.id)
                        }
                        change.isNew -> {
                            newItems.add(
                                change.item.copy(
                                    id = "",
                                    quantity = change.newQuantity,
                                    lastUpdatedBy = user.uid
                                )
                            )
                        }
                        else -> {
                            updates.add(
                                change.item.copy(
                                    quantity = change.newQuantity,
                                    lastUpdatedBy = user.uid
                                )
                            )
                        }
                    }
                }

                val history = InventoryHistory(
                    userId = user.uid,
                    userName = userName,
                    comment = comment,
                    items = historyItems
                )

                inventoryUseCases.saveTransaction(boardId, updates, newItems, deletions, history)
                _pendingChanges.value = emptyMap()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun discardChanges() {
        _pendingChanges.value = emptyMap()
    }

    fun deleteItem(itemId: String) {
        val pending = _pendingChanges.value.toMutableMap()

        // If it's a pending new item, just remove from pending entirely
        if (pending[itemId]?.isNew == true) {
            pending.remove(itemId)
            _pendingChanges.value = pending
            return
        }

        // Mark existing item as pending deletion
        val fsItem = _firestoreItems.value.find { it.id == itemId } ?: return
        pending[itemId] = PendingChange(
            item = fsItem,
            originalQuantity = fsItem.quantity,
            newQuantity = 0.0,
            isNew = false,
            isDeleted = true
        )
        _pendingChanges.value = pending
    }

    /**
     * Set an exact quantity and/or unit locally. Used by the edit dialog.
     */
    fun setLocalQuantityAndUnit(itemId: String, newQuantity: Double, newUnit: String) {
        val pending = _pendingChanges.value.toMutableMap()
        val existing = pending[itemId]

        if (existing != null) {
            val updatedItem = existing.item.copy(unit = newUnit)
            pending[itemId] = existing.copy(item = updatedItem, newQuantity = newQuantity.coerceAtLeast(0.0))
        } else {
            val fsItem = _firestoreItems.value.find { it.id == itemId } ?: return
            pending[itemId] = PendingChange(
                item = fsItem.copy(unit = newUnit),
                originalQuantity = fsItem.quantity,
                newQuantity = newQuantity.coerceAtLeast(0.0),
                isNew = false
            )
        }

        // Remove if back to original
        val change = pending[itemId]
        if (change != null && !change.isNew) {
            val fsItem = _firestoreItems.value.find { it.id == itemId }
            if (fsItem != null && change.newQuantity == fsItem.quantity && change.item.unit == fsItem.unit) {
                pending.remove(itemId)
            }
        }

        _pendingChanges.value = pending
    }

    fun undoDeleteItem(itemId: String) {
        val pending = _pendingChanges.value.toMutableMap()
        if (pending[itemId]?.isDeleted == true) {
            pending.remove(itemId)
            _pendingChanges.value = pending
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun getLowStockItems(): List<InventoryItem> {
        return _displayItems.value.filter { it.minQuantity > 0 && it.quantity <= it.minQuantity }
    }

    private fun mergeDisplayItems(
        fsItems: List<InventoryItem>,
        pending: Map<String, PendingChange>
    ): List<InventoryItem> {
        // Start with Firestore items, applying pending changes (including deleted — kept for display)
        val merged = fsItems.map { item ->
            val change = pending[item.id]
            if (change != null && !change.isNew) {
                item.copy(quantity = change.newQuantity)
            } else {
                item
            }
        }.toMutableList()

        // Append new pending items
        pending.values
            .filter { it.isNew }
            .forEach { change ->
                merged.add(change.item.copy(quantity = change.newQuantity))
            }

        return merged
    }
}
