package com.mariustanke.domotask.domain.usecase.inventory

import com.mariustanke.domotask.domain.model.Product
import com.mariustanke.domotask.domain.repository.ProductRepository
import javax.inject.Inject

class SearchProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(query: String): List<Product> =
        repository.searchProducts(query)
}
