package com.mariustanke.domotask.domain.usecase.auth

import com.mariustanke.domotask.domain.model.User
import com.mariustanke.domotask.domain.repository.UserRepository
import javax.inject.Inject

class CreateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User) {
        return userRepository.saveUserToFirestore(user)
    }
}