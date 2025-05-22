package com.mariustanke.domotask.domain.usecase.board

import com.mariustanke.domotask.domain.model.Status
import com.mariustanke.domotask.domain.repository.BoardRepository

class CreateBoardStatusUseCase (
    private val repository: BoardRepository
) {
    suspend operator fun invoke(boardId: String, status: Status): String =
        repository.createStatus(boardId, status)
}