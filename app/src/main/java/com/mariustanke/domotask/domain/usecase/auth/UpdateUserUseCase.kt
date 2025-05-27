package com.mariustanke.domotask.domain.usecase.auth

import com.mariustanke.domotask.domain.model.User
import com.mariustanke.domotask.domain.repository.UserRepository

class UpdateUserUseCase constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User) {
        return userRepository.updateUser(user)
    }
}