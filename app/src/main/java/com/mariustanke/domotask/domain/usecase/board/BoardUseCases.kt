package com.mariustanke.domotask.domain.usecase.board

data class BoardUseCases(
    val getBoards: GetBoardsUseCase,
    val getBoard: GetBoardUseCase,
    val createBoard: CreateBoardUseCase,
    val updateBoard: UpdateBoardUseCase,
    val deleteBoard: DeleteBoardUseCase,

    val createTicket: CreateTicketUseCase,
    val updateTicket: UpdateTicketUseCase,
    val deleteTicket: DeleteTicketUseCase,
    val getTickets: GetTicketsUseCase,

    val createBoardStatus: CreateBoardStatusUseCase,
    val getBoardStatus: GetBoardStatusesUseCase,
    val updateBoardStatus: UpdateBoardStatusUseCase,
    val deleteBoardStatus: DeleteBoardStatusUseCase,

    val inviteUserToBoardUseCase: InviteUserToBoardUseCase,
    val acceptInvitationUseCase: AcceptInvitationUseCase,
    val removeMemberFromBoardUseCase: RemoveMemberFromBoardUseCase,
    val rejectInvitationUseCase: RejectInvitationUseCase
)
