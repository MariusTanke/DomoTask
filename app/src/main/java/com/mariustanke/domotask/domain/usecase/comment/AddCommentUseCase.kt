package com.mariustanke.domotask.domain.usecase.comment

import com.mariustanke.domotask.domain.model.Comment
import com.mariustanke.domotask.domain.repository.BoardRepository

class AddCommentUseCase(
    private val repository: BoardRepository
) {
    suspend operator fun invoke(boardId: String, ticketId: String, comment: Comment): String {
        return repository.addComment(boardId, ticketId, comment)
    }
}