package com.mariustanke.domotask.domain.usecase.inventory

import com.mariustanke.domotask.domain.model.Product
import com.mariustanke.domotask.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> =
        repository.getProducts()
}
