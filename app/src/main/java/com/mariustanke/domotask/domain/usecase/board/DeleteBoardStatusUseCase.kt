package com.mariustanke.domotask.domain.usecase.board

import com.mariustanke.domotask.domain.repository.BoardRepository
import javax.inject.Inject

class DeleteBoardStatusUseCase @Inject constructor(
    private val repository: BoardRepository
) {
    suspend operator fun invoke(boardId: String, statusId: String) {
        repository.deleteStatus(boardId, statusId)
    }
}
