package com.mariustanke.domotask.di

import com.mariustanke.domotask.domain.repository.AuthRepository
import com.mariustanke.domotask.domain.repository.BoardRepository
import com.mariustanke.domotask.domain.repository.UserRepository
import com.mariustanke.domotask.domain.usecase.auth.CreateUserUseCase
import com.mariustanke.domotask.domain.usecase.auth.UserUseCases
import com.mariustanke.domotask.domain.usecase.auth.GetUserUseCase
import com.mariustanke.domotask.domain.usecase.auth.UpdateFcmTokenUseCase
import com.mariustanke.domotask.domain.usecase.auth.UpdateUserUseCase
import com.mariustanke.domotask.domain.usecase.board.AcceptInvitationUseCase
import com.mariustanke.domotask.domain.usecase.board.BoardUseCases
import com.mariustanke.domotask.domain.usecase.board.CreateBoardStatusUseCase
import com.mariustanke.domotask.domain.usecase.board.CreateBoardUseCase
import com.mariustanke.domotask.domain.usecase.board.CreateTicketUseCase
import com.mariustanke.domotask.domain.usecase.board.DeleteBoardStatusUseCase
import com.mariustanke.domotask.domain.usecase.board.DeleteBoardUseCase
import com.mariustanke.domotask.domain.usecase.board.DeleteTicketUseCase
import com.mariustanke.domotask.domain.usecase.board.GetBoardStatusesUseCase
import com.mariustanke.domotask.domain.usecase.board.GetBoardUseCase
import com.mariustanke.domotask.domain.usecase.board.UpdateTicketUseCase
import com.mariustanke.domotask.domain.usecase.board.GetBoardsUseCase
import com.mariustanke.domotask.domain.usecase.board.GetTicketsUseCase
import com.mariustanke.domotask.domain.usecase.board.InviteUserToBoardUseCase
import com.mariustanke.domotask.domain.usecase.board.RejectInvitationUseCase
import com.mariustanke.domotask.domain.usecase.board.RemoveMemberFromBoardUseCase
import com.mariustanke.domotask.domain.usecase.board.UpdateBoardStatusUseCase
import com.mariustanke.domotask.domain.usecase.board.UpdateBoardUseCase
import com.mariustanke.domotask.domain.usecase.comment.AddCommentUseCase
import com.mariustanke.domotask.domain.usecase.comment.CommentUseCases
import com.mariustanke.domotask.domain.usecase.comment.DeleteCommentUseCase
import com.mariustanke.domotask.domain.usecase.comment.GetCommentsUseCase
import com.mariustanke.domotask.domain.usecase.comment.UpdateCommentUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideBoardUseCases(
        boardRepository: BoardRepository,
        userRepository: UserRepository
    ): BoardUseCases {
        return BoardUseCases(
            getBoards = GetBoardsUseCase(boardRepository),
            getBoard = GetBoardUseCase(boardRepository),
            createBoard = CreateBoardUseCase(boardRepository),
            createTicket = CreateTicketUseCase(boardRepository),
            updateTicket = UpdateTicketUseCase(boardRepository),
            deleteTicket = DeleteTicketUseCase(boardRepository),
            getTickets = GetTicketsUseCase(boardRepository),
            getBoardStatus = GetBoardStatusesUseCase(boardRepository),
            createBoardStatus = CreateBoardStatusUseCase(boardRepository),
            updateBoardStatus = UpdateBoardStatusUseCase(boardRepository),
            deleteBoardStatus = DeleteBoardStatusUseCase(boardRepository),
            updateBoard = UpdateBoardUseCase(boardRepository),
            deleteBoard = DeleteBoardUseCase(boardRepository),
            inviteUserToBoardUseCase = InviteUserToBoardUseCase(userRepository),
            acceptInvitationUseCase = AcceptInvitationUseCase(userRepository, boardRepository),
            removeMemberFromBoardUseCase = RemoveMemberFromBoardUseCase(boardRepository),
            rejectInvitationUseCase = RejectInvitationUseCase(userRepository)
        )
    }

    @Provides
    @Singleton
    fun provideCommentUseCases(
        boardRepository: BoardRepository
    ): CommentUseCases {
        return CommentUseCases(
            getComments = GetCommentsUseCase(boardRepository),
            addComment = AddCommentUseCase(boardRepository),
            updateComment = UpdateCommentUseCase(boardRepository),
            deleteComment = DeleteCommentUseCase(boardRepository)
        )
    }

    @Provides
    @Singleton
    fun provideAuthUseCases(
        userRepository: UserRepository
    ): UserUseCases {
        return UserUseCases(
            getUser = GetUserUseCase(userRepository),
            createUser = CreateUserUseCase(userRepository),
            updateUser = UpdateUserUseCase(userRepository),
            updateFcmToken = UpdateFcmTokenUseCase(userRepository)
        )
    }
}