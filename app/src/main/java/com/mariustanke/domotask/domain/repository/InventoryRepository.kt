package com.mariustanke.domotask.domain.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mariustanke.domotask.domain.model.HistoryItemChange
import com.mariustanke.domotask.domain.model.InventoryHistory
import com.mariustanke.domotask.domain.model.InventoryItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InventoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val boardsCollection = firestore.collection("boards")

    private fun itemsCollection(boardId: String) =
        boardsCollection.document(boardId).collection("inventoryItems")

    private fun historyCollection(boardId: String) =
        boardsCollection.document(boardId).collection("inventoryHistory")

    // ─── Inventory Items ────────────────────────────────────────────────

    fun getInventoryItems(boardId: String): Flow<List<InventoryItem>> = callbackFlow {
        val listener = itemsCollection(boardId)
            .orderBy("productName")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val items = snapshot?.documents
                    ?.mapNotNull { it.toObject(InventoryItem::class.java)?.copy(id = it.id) }
                    ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getInventoryItem(boardId: String, itemId: String): InventoryItem? {
        return itemsCollection(boardId).document(itemId)
            .get().await()
            .toObject(InventoryItem::class.java)
            ?.copy(id = itemId)
    }

    suspend fun addInventoryItem(boardId: String, item: InventoryItem): String {
        val docRef = itemsCollection(boardId).document()
        val withId = item.copy(id = docRef.id)
        docRef.set(withId).await()
        return docRef.id
    }

    suspend fun updateInventoryItem(boardId: String, item: InventoryItem) {
        itemsCollection(boardId)
            .document(item.id)
            .set(item)
            .await()
    }

    suspend fun deleteInventoryItem(boardId: String, itemId: String) {
        itemsCollection(boardId)
            .document(itemId)
            .delete()
            .await()
    }

    /**
     * Atomically saves a batch of inventory changes (updates + new items) and
     * records a single grouped history entry. Uses a Firestore batch write.
     */
    suspend fun saveTransaction(
        boardId: String,
        updates: List<InventoryItem>,
        newItems: List<InventoryItem>,
        deletions: List<String>,
        history: InventoryHistory
    ) {
        val batch = firestore.batch()
        val now = System.currentTimeMillis()

        // Update existing items
        for (item in updates) {
            val ref = itemsCollection(boardId).document(item.id)
            batch.set(ref, item.copy(lastUpdatedAt = now))
        }

        // Add new items
        for (item in newItems) {
            val ref = if (item.id.isNotBlank()) {
                itemsCollection(boardId).document(item.id)
            } else {
                itemsCollection(boardId).document()
            }
            batch.set(ref, item.copy(id = ref.id, lastUpdatedAt = now))
        }

        // Delete items
        for (itemId in deletions) {
            val ref = itemsCollection(boardId).document(itemId)
            batch.delete(ref)
        }

        // Record grouped history
        val historyRef = historyCollection(boardId).document()
        batch.set(historyRef, history.copy(id = historyRef.id, createdAt = now))

        batch.commit().await()
    }

    // ─── History ────────────────────────────────────────────────────────

    fun getHistory(boardId: String): Flow<List<InventoryHistory>> = callbackFlow {
        val listener = historyCollection(boardId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val history = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(InventoryHistory::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(history)
            }
        awaitClose { listener.remove() }
    }

    fun getItemHistory(boardId: String, itemId: String): Flow<List<InventoryHistory>> = callbackFlow {
        val listener = historyCollection(boardId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val history = snapshot?.documents?.mapNotNull { doc ->
                    val entry = doc.toObject(InventoryHistory::class.java)?.copy(id = doc.id)
                    // Filter: only return entries that contain changes for this item
                    entry?.let {
                        val filtered = it.items.filter { change -> change.inventoryItemId == itemId }
                        if (filtered.isNotEmpty()) it.copy(items = filtered) else null
                    }
                } ?: emptyList()
                trySend(history)
            }
        awaitClose { listener.remove() }
    }
}
