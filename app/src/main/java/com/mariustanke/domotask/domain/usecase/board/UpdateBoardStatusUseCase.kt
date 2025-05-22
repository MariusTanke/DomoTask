package com.mariustanke.domotask.domain.usecase.board

import com.mariustanke.domotask.domain.model.Status
import com.mariustanke.domotask.domain.repository.BoardRepository
import javax.inject.Inject

class UpdateBoardStatusUseCase @Inject constructor(
    private val repository: BoardRepository
) {
    suspend operator fun invoke(boardId: String, status: Status) {
        repository.updateStatus(boardId, status)
    }
}
