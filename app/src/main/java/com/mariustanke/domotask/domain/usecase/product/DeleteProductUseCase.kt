package com.mariustanke.domotask.domain.usecase.product

import com.mariustanke.domotask.domain.repository.ProductRepository
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: String) =
        repository.deleteProduct(productId)
}
