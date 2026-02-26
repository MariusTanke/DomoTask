package com.mariustanke.domotask.di

import com.mariustanke.domotask.domain.repository.AuthRepository
import com.mariustanke.domotask.domain.repository.BoardRepository
import com.mariustanke.domotask.domain.repository.InventoryRepository
import com.mariustanke.domotask.domain.repository.ProductRepository
import com.mariustanke.domotask.domain.repository.UserRepository
import com.mariustanke.domotask.domain.usecase.auth.CreateUserUseCase
import com.mariustanke.domotask.domain.usecase.auth.GetUserFlowUseCase
import com.mariustanke.domotask.domain.usecase.auth.UserUseCases
import com.mariustanke.domotask.domain.usecase.auth.GetUserUseCase
import com.mariustanke.domotask.domain.usecase.auth.UpdateFcmTokenUseCase
import com.mariustanke.domotask.domain.usecase.auth.UpdateUserUseCase
import com.mariustanke.domotask.domain.usecase.board.AcceptInvitationUseCase
import com.mariustanke.domotask.domain.usecase.board.BoardUseCases
import com.mariustanke.domotask.domain.usecase.board.CreateBoardStatusUseCase
import com.mariustanke.domotask.domain.usecase.board.CreateBoardUseCase
import com.mariustanke.domotask.domain.usecase.board.CreateSubTicketUseCase
import com.mariustanke.domotask.domain.usecase.board.CreateTicketUseCase
import com.mariustanke.domotask.domain.usecase.board.DeleteBoardStatusUseCase
import com.mariustanke.domotask.domain.usecase.board.DeleteBoardUseCase
import com.mariustanke.domotask.domain.usecase.board.DeleteTicketUseCase
import com.mariustanke.domotask.domain.usecase.board.GetBoardStatusesUseCase
import com.mariustanke.domotask.domain.usecase.board.GetBoardUseCase
import com.mariustanke.domotask.domain.usecase.board.UpdateTicketUseCase
import com.mariustanke.domotask.domain.usecase.board.GetBoardsUseCase
import com.mariustanke.domotask.domain.usecase.board.GetTicketUseCase
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
import com.mariustanke.domotask.domain.usecase.inventory.AddInventoryItemUseCase
import com.mariustanke.domotask.domain.usecase.inventory.DeleteInventoryItemUseCase
import com.mariustanke.domotask.domain.usecase.inventory.GetInventoryHistoryUseCase
import com.mariustanke.domotask.domain.usecase.inventory.GetInventoryItemsUseCase
import com.mariustanke.domotask.domain.usecase.inventory.GetItemHistoryUseCase
import com.mariustanke.domotask.domain.usecase.inventory.GetProductsUseCase
import com.mariustanke.domotask.domain.usecase.inventory.InventoryUseCases
import com.mariustanke.domotask.domain.usecase.inventory.SaveInventoryTransactionUseCase
import com.mariustanke.domotask.domain.usecase.inventory.SearchProductsUseCase
import com.mariustanke.domotask.domain.usecase.product.CreateProductUseCase
import com.mariustanke.domotask.domain.usecase.product.DeleteProductUseCase
import com.mariustanke.domotask.domain.usecase.product.GetAllProductsUseCase
import com.mariustanke.domotask.domain.usecase.product.GetProductsPaginatedUseCase
import com.mariustanke.domotask.domain.usecase.product.ProductUseCases
import com.mariustanke.domotask.domain.usecase.product.UpdateProductUseCase
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
            createSubTicket = CreateSubTicketUseCase(boardRepository),
            updateTicket = UpdateTicketUseCase(boardRepository),
            deleteTicket = DeleteTicketUseCase(boardRepository),
            getTickets = GetTicketsUseCase(boardRepository),
            getTicket = GetTicketUseCase(boardRepository),
            getBoardStatus = GetBoardStatusesUseCase(boardRepository),
            createBoardStatus = CreateBoardStatusUseCase(boardRepository),
            updateBoardStatus = UpdateBoardStatusUseCase(boardRepository),
            deleteBoardStatus = DeleteBoardStatusUseCase(boardRepository),
            updateBoard = UpdateBoardUseCase(boardRepository),
            deleteBoard = DeleteBoardUseCase(boardRepository),
            inviteUserToBoardUseCase = InviteUserToBoardUseCase(userRepository),
            acceptInvitationUseCase = AcceptInvitationUseCase(userRepository, boardRepository),
            removeMemberFromBoardUseCase = RemoveMemberFromBoardUseCase(boardRepository),
            rejectInvitationUseCase = RejectInvitationUseCase(userRepository),
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
            getUserFlow = GetUserFlowUseCase(userRepository),
            createUser = CreateUserUseCase(userRepository),
            updateUser = UpdateUserUseCase(userRepository),
            updateFcmToken = UpdateFcmTokenUseCase(userRepository)
        )
    }

    @Provides
    @Singleton
    fun provideProductUseCases(
        productRepository: ProductRepository
    ): ProductUseCases {
        return ProductUseCases(
            getProducts = GetAllProductsUseCase(productRepository),
            getProductsPaginated = GetProductsPaginatedUseCase(productRepository),
            createProduct = CreateProductUseCase(productRepository),
            updateProduct = UpdateProductUseCase(productRepository),
            deleteProduct = DeleteProductUseCase(productRepository)
        )
    }

    @Provides
    @Singleton
    fun provideInventoryUseCases(
        inventoryRepository: InventoryRepository,
        productRepository: ProductRepository
    ): InventoryUseCases {
        return InventoryUseCases(
            getInventoryItems = GetInventoryItemsUseCase(inventoryRepository),
            addInventoryItem = AddInventoryItemUseCase(inventoryRepository),
            saveTransaction = SaveInventoryTransactionUseCase(inventoryRepository),
            deleteInventoryItem = DeleteInventoryItemUseCase(inventoryRepository),
            getInventoryHistory = GetInventoryHistoryUseCase(inventoryRepository),
            getItemHistory = GetItemHistoryUseCase(inventoryRepository),
            searchProducts = SearchProductsUseCase(productRepository),
            getProducts = GetProductsUseCase(productRepository)
        )
    }
}