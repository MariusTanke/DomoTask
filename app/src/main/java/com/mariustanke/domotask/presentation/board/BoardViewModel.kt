package com.mariustanke.domotask.presentation.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mariustanke.domotask.domain.model.Board
import com.mariustanke.domotask.domain.model.Status
import com.mariustanke.domotask.domain.model.Ticket
import com.mariustanke.domotask.domain.model.User
import com.mariustanke.domotask.domain.usecase.auth.UserUseCases
import com.mariustanke.domotask.domain.usecase.board.BoardUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val userUseCases: UserUseCases,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _board = MutableStateFlow<Board?>(null)
    val board: StateFlow<Board?> = _board

    private val _tickets = MutableStateFlow<List<Ticket>>(emptyList())
    val tickets: StateFlow<List<Ticket>> = _tickets.asStateFlow()

    private val _statuses = MutableStateFlow<List<Status>>(emptyList())
    val statuses: StateFlow<List<Status>> = _statuses.asStateFlow()

    private val _members = MutableStateFlow<List<User>>(emptyList())
    val members: StateFlow<List<User>> = _members.asStateFlow()

    private val _inviteState = MutableStateFlow<Result<Unit>?>(null)
    val inviteState: StateFlow<Result<Unit>?> = _inviteState

    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    fun clearInviteState() {
        _inviteState.value = null
    }

    fun loadBoard(boardId: String) {
        viewModelScope.launch {
            boardUseCases.getTickets(boardId).collect { _tickets.value = it }
        }
        viewModelScope.launch {
            boardUseCases.getBoardStatus(boardId).collect { _statuses.value = it }
        }
        viewModelScope.launch {
            boardUseCases.getBoard(boardId).collect { board ->
                _board.value = board
                board.members
                    .mapNotNull { memberId ->
                        userUseCases.getUser(memberId)
                    }
                    .let { usersList ->
                        _members.value = usersList
                    }
            }
        }
    }

    fun createTicket(boardId: String, ticket: Ticket) {
        viewModelScope.launch {
            boardUseCases.createTicket(boardId, ticket)
        }
    }

    fun updateTicket(boardId: String, ticket: Ticket) {
        viewModelScope.launch {
            boardUseCases.updateTicket(boardId, ticket)
        }
    }

    fun deleteTicket(boardId: String, ticketId: String) {
        viewModelScope.launch {
            boardUseCases.deleteTicket(boardId, ticketId)
        }
    }

    fun inviteMember(boardId: String, code: String) {
        viewModelScope.launch {
            _inviteState.value = boardUseCases.inviteUserToBoardUseCase(boardId, code)
        }
    }

    fun removeMember(boardId: String, userId: String, removeUserTickets: Boolean) {
        viewModelScope.launch {
            boardUseCases.removeMemberFromBoardUseCase(boardId, userId)
            _members.update {
                list -> list.filterNot { it.id == userId }
            }

            val userTickets = tickets.value.filter { it.createdBy == userId }

            if (removeUserTickets) {
                userTickets.forEach { ticket ->
                    boardUseCases.deleteTicket(boardId, ticket.id)
                }

            } else {
                if (board.value != null) {
                    userTickets.forEach { ticket ->
                        boardUseCases.updateTicket(boardId, ticket.copy(createdBy = board.value!!.createdBy))
                    }
                }
            }
        }
    }

    private fun syncOrdersAndPersist(boardId: String, modifiedList: List<Status>) = viewModelScope.launch {
        val reindexed = modifiedList
            .sortedBy { it.order }
            .mapIndexed { index, s ->
                if (s.order != index + 1) s.copy(order = index + 1) else s
            }

        reindexed.forEach { status ->
            boardUseCases.updateBoardStatus(
                boardId,
                status
            )
        }
    }

    fun createBoardStatus(boardId: String, newStatusParam: Status) {
        viewModelScope.launch {
            val current = statuses.value
                .sortedBy { it.order }
                .toMutableList()

            val insertIndex = (newStatusParam.order - 1).coerceIn(0, current.size)
            current.add(insertIndex, newStatusParam)
            syncOrdersAndPersist(boardId, current)
        }
    }

    fun updateBoardStatus(boardId: String, updatedStatus: Status) {
        viewModelScope.launch {
            val current = statuses.value
                .sortedBy { it.order }
                .toMutableList()

            val oldIndex = current.indexOfFirst { it.id == updatedStatus.id }
            if (oldIndex == -1) return@launch

            current.removeAt(oldIndex)
            val newIndex = (updatedStatus.order - 1).coerceIn(0, current.size)
            current.add(newIndex, updatedStatus)

            syncOrdersAndPersist(boardId, current)
        }
    }

    fun removeBoardStatus(boardId: String, statusId: String) {
        viewModelScope.launch {
            val current = statuses.value
                .sortedBy { it.order }
                .toMutableList()
            current.removeAll { it.id == statusId }
            boardUseCases.deleteBoardStatus(boardId, statusId)
            syncOrdersAndPersist(boardId, current)
        }
    }

    fun deleteBoard(boardId: String) {
        viewModelScope.launch {
            boardUseCases.deleteBoard(boardId)
        }
    }
}