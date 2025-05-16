package com.mariustanke.domotask.domain.usecase.comment

import com.mariustanke.domotask.domain.repository.BoardRepository

class DeleteCommentUseCase(
    private val repository: BoardRepository
) {
    suspend operator fun invoke(boardId: String, ticketId: String, commentId: String) {
        repository.deleteComment(boardId, ticketId, commentId)
    }
}