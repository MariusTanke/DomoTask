package com.mariustanke.domotask.domain.usecase.board

data class BoardUseCases(
    val getBoards: GetBoardsUseCase,
    val createBoard: CreateBoardUseCase,
    val createTicket: CreateTicketUseCase,
    val updateTicket: UpdateTicketUseCase,
    val deleteTicket: DeleteTicketUseCase,
    val getTickets: GetTicketsUseCase
)
