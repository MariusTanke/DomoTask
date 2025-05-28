package com.mariustanke.domotask.domain.usecase.board

import com.mariustanke.domotask.domain.repository.BoardRepository
import javax.inject.Inject

class GetTicketUseCase @Inject constructor(
    private val repository: BoardRepository
) {
    operator fun invoke(boardId: String, ticketId: String) = repository.getTicket(boardId, ticketId)
}
