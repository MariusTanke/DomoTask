package com.mariustanke.domotask.domain.usecase.board

import com.mariustanke.domotask.domain.model.Status
import com.mariustanke.domotask.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBoardStatusesUseCase @Inject constructor(
    private val repository: BoardRepository
) {
    operator fun invoke(boardId: String): Flow<List<Status>> =
        repository.getStatuses(boardId)
}