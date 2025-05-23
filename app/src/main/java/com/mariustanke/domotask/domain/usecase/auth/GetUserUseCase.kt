package com.mariustanke.domotask.domain.usecase.auth

import com.mariustanke.domotask.domain.model.User
import com.mariustanke.domotask.domain.repository.UserRepository
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uid: String): User? {
        return userRepository.getUserById(uid)
    }
}
