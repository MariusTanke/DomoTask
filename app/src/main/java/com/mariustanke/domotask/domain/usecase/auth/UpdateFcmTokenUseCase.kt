package com.mariustanke.domotask.domain.usecase.auth

import com.mariustanke.domotask.domain.repository.UserRepository

class UpdateFcmTokenUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uid: String, token: String) {
        userRepository.updateFcmToken(uid, token)
    }
}

