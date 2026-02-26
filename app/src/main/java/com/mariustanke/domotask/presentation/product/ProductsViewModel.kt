package com.mariustanke.domotask.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.mariustanke.domotask.domain.enums.ProductCategory
import com.mariustanke.domotask.domain.model.Product
import com.mariustanke.domotask.domain.usecase.product.ProductUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProductSortOption(val label: String) {
    NAME_ASC("Nombre A-Z"),
    NAME_DESC("Nombre Z-A"),
    CATEGORY("Categor\u00eda"),
    NEWEST("M\u00e1s recientes"),
    OLDEST("M\u00e1s antiguos")
}

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val productUseCases: ProductUseCases
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 30
    }

    private val _allLoadedProducts = MutableStateFlow<List<Product>>(emptyList())

    private val _filteredProducts = MutableStateFlow<List<Product>>(emptyList())
    val filteredProducts: StateFlow<List<Product>> = _filteredProducts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<ProductCategory>>(emptySet())
    val selectedCategories: StateFlow<Set<ProductCategory>> = _selectedCategories.asStateFlow()

    private val _sortOption = MutableStateFlow(ProductSortOption.NAME_ASC)
    val sortOption: StateFlow<ProductSortOption> = _sortOption.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var lastDocument: DocumentSnapshot? = null

    init {
        loadFirstPage()
        viewModelScope.launch {
            combine(_allLoadedProducts, _searchQuery, _selectedCategories, _sortOption) { products, query, cats, sort ->
                applyFiltersAndSort(products, query, cats, sort)
            }.collect { _filteredProducts.value = it }
        }
    }

    private fun loadFirstPage() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                lastDocument = null
                val result = productUseCases.getProductsPaginated(PAGE_SIZE, null)
                _allLoadedProducts.value = result.products
                lastDocument = result.lastDocument
                _hasMore.value = result.hasMore
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {
        if (!_hasMore.value || _isLoadingMore.value || _isLoading.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val result = productUseCases.getProductsPaginated(PAGE_SIZE, lastDocument)
                _allLoadedProducts.value = _allLoadedProducts.value + result.products
                lastDocument = result.lastDocument
                _hasMore.value = result.hasMore
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    private fun refresh() {
        loadFirstPage()
    }

    fun createProduct(name: String, description: String, category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val product = Product(
                    name = name.trim(),
                    description = description.trim(),
                    category = category
                )
                productUseCases.createProduct(product)
                refresh()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productUseCases.updateProduct(product)
                refresh()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productUseCases.deleteProduct(productId)
                refresh()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleCategory(category: ProductCategory) {
        val current = _selectedCategories.value.toMutableSet()
        if (category in current) current.remove(category) else current.add(category)
        _selectedCategories.value = current
    }

    fun clearCategoryFilters() {
        _selectedCategories.value = emptySet()
    }

    fun setSortOption(option: ProductSortOption) {
        _sortOption.value = option
    }

    fun clearError() {
        _error.value = null
    }

    private fun applyFiltersAndSort(
        products: List<Product>,
        query: String,
        categories: Set<ProductCategory>,
        sort: ProductSortOption
    ): List<Product> {
        var result = products

        if (query.isNotBlank()) {
            result = result.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
            }
        }

        if (categories.isNotEmpty()) {
            result = result.filter { ProductCategory.fromValue(it.category) in categories }
        }

        result = when (sort) {
            ProductSortOption.NAME_ASC -> result.sortedBy { it.name.lowercase() }
            ProductSortOption.NAME_DESC -> result.sortedByDescending { it.name.lowercase() }
            ProductSortOption.CATEGORY -> result.sortedBy { it.category }
            ProductSortOption.NEWEST -> result.sortedByDescending { it.createdAt }
            ProductSortOption.OLDEST -> result.sortedBy { it.createdAt }
        }

        return result
    }
}
