package com.mariustanke.domotask.domain.usecase.board

import com.mariustanke.domotask.domain.repository.BoardRepository
import javax.inject.Inject

class RemoveMemberFromBoardUseCase @Inject constructor(
    private val repository: BoardRepository
) {
    suspend operator fun invoke(boardId: String, userId: String): Unit =
        repository.removeMemberFromBoard(boardId, userId)
}