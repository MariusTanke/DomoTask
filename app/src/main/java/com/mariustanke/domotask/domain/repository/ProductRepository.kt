package com.mariustanke.domotask.domain.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mariustanke.domotask.domain.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val productsCollection = firestore.collection("products")

    fun getProducts(): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(); return@addSnapshotListener }
                val products = snapshot?.documents
                    ?.mapNotNull { it.toObject(Product::class.java)?.copy(id = it.id) }
                    ?: emptyList()
                trySend(products)
            }
        awaitClose { listener.remove() }
    }

    fun getProductsByCategory(category: String): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .whereEqualTo("active", true)
            .whereEqualTo("category", category)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(); return@addSnapshotListener }
                val products = snapshot?.documents
                    ?.mapNotNull { it.toObject(Product::class.java)?.copy(id = it.id) }
                    ?: emptyList()
                trySend(products)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getProductById(productId: String): Product? {
        return productsCollection.document(productId)
            .get().await()
            .toObject(Product::class.java)
            ?.copy(id = productId)
    }

    suspend fun searchProducts(query: String): List<Product> {
        val snapshot = productsCollection
            .whereEqualTo("active", true)
            .get().await()
        return snapshot.documents
            .mapNotNull { it.toObject(Product::class.java)?.copy(id = it.id) }
            .filter { it.name.contains(query, ignoreCase = true) }
    }

    suspend fun createProduct(product: Product): String {
        val docRef = productsCollection.document()
        val withId = product.copy(id = docRef.id)
        docRef.set(withId).await()
        return docRef.id
    }
}
