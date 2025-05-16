package com.mariustanke.domotask.domain.usecase.comment

import com.mariustanke.domotask.domain.model.Comment
import com.mariustanke.domotask.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow

class GetCommentsUseCase(
    private val repository: BoardRepository
) {
    operator fun invoke(boardId: String, ticketId: String): Flow<List<Comment>> {
        return repository.getComments(boardId, ticketId)
    }
}
