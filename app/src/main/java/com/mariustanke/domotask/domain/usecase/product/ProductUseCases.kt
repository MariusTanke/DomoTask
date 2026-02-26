package com.mariustanke.domotask.domain.usecase.product

data class ProductUseCases(
    val getProducts: GetAllProductsUseCase,
    val getProductsPaginated: GetProductsPaginatedUseCase,
    val createProduct: CreateProductUseCase,
    val updateProduct: UpdateProductUseCase,
    val deleteProduct: DeleteProductUseCase
)
