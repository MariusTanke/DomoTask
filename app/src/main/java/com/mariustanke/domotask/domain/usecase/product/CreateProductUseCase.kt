package com.mariustanke.domotask.domain.usecase.product

import com.mariustanke.domotask.domain.model.Product
import com.mariustanke.domotask.domain.repository.ProductRepository
import javax.inject.Inject

class CreateProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(product: Product): String =
        repository.createProduct(product)
}
