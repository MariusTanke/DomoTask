package com.mariustanke.domotask.domain.usecase.board

import com.mariustanke.domotask.domain.model.Ticket
import com.mariustanke.domotask.domain.repository.BoardRepository
import javax.inject.Inject

class CreateSubTicketUseCase @Inject constructor(
    private val repository: BoardRepository
) {
    suspend operator fun invoke(boardId: String, ticketId: String, ticket: Ticket): String {
        return repository.addSubTicket(boardId, ticketId, ticket)
    }
}