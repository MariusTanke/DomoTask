package com.mariustanke.domotask.domain.usecase.board

import com.mariustanke.domotask.domain.repository.UserRepository
import javax.inject.Inject

class InviteUserToBoardUseCase @Inject constructor(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(boardId: String, code: String): Result<Unit> {
        val user = userRepo.getUserByInvitationCode(code)
            ?: return Result.failure(Exception("Código no válido"))

        user.invitations.let { invs ->
            if (invs.contains(boardId)) {
                return Result.failure(Exception("Ya tiene invitación pendiente"))
            }
        }

        userRepo.addInvitation(user.id, boardId)
        return Result.success(Unit)
    }
}