package com.mariustanke.domotask.domain.usecase.board

import com.mariustanke.domotask.domain.repository.BoardRepository
import com.mariustanke.domotask.domain.repository.UserRepository
import javax.inject.Inject

class AcceptInvitationUseCase @Inject constructor(
    private val userRepo: UserRepository,
    private val boardRepo: BoardRepository
) {

    suspend operator fun invoke(boardId: String, userId: String): Result<Unit> {
        userRepo.removeInvitation(userId, boardId)
        boardRepo.addMemberToBoard(boardId, userId)
        return Result.success(Unit)
    }
}