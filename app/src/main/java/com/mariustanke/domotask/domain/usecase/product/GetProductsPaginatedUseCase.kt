package com.mariustanke.domotask.domain.usecase.product

import com.google.firebase.firestore.DocumentSnapshot
import com.mariustanke.domotask.domain.repository.PaginatedResult
import com.mariustanke.domotask.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductsPaginatedUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(pageSize: Int, lastDocument: DocumentSnapshot?): PaginatedResult =
        repository.getProductsPaginated(pageSize, lastDocument)
}
