package com.mariustanke.domotask.domain.usecase.auth

import com.mariustanke.domotask.domain.repository.UserRepository
import javax.inject.Inject

class GetUserFlowUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(userId: String) = repository.getUserFlow(userId)
}
