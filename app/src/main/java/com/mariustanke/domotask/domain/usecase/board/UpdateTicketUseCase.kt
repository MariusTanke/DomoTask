package com.mariustanke.domotask.domain.usecase.board

import com.mariustanke.domotask.domain.model.Ticket
import com.mariustanke.domotask.domain.repository.BoardRepository
import javax.inject.Inject

class UpdateTicketUseCase @Inject constructor(
    private val repository: BoardRepository
) {
    suspend operator fun invoke(boardId: String, ticket: Ticket) {
        repository.updateTicket(boardId, ticket)
    }
}