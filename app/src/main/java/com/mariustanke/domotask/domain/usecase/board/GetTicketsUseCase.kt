package com.mariustanke.domotask.domain.usecase.board

import com.mariustanke.domotask.domain.repository.BoardRepository
import javax.inject.Inject

class GetTicketsUseCase @Inject constructor(
    private val repository: BoardRepository
) {
    operator fun invoke(boardId: String) = repository.getTickets(boardId)
}
