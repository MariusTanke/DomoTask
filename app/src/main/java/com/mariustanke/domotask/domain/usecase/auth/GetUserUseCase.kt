package com.mariustanke.domotask.domain.usecase.auth

import com.mariustanke.domotask.domain.model.User
import com.mariustanke.domotask.domain.repository.AuthRepository
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(uid: String): User? {
        return authRepository.getUserById(uid)
    }
}
