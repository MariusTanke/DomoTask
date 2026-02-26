package com.mariustanke.domotask.domain.usecase.product

import com.mariustanke.domotask.domain.model.Product
import com.mariustanke.domotask.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> =
        repository.getProducts()
}
