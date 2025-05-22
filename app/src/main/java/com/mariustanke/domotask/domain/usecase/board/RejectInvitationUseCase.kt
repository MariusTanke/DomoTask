package com.mariustanke.domotask.domain.usecase.board

import com.mariustanke.domotask.domain.repository.UserRepository
import javax.inject.Inject

class RejectInvitationUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(boardId: String, userId: String): Result<Unit> =
        runCatching {
            userRepository.removeInvitation(userId, boardId)
        }
}