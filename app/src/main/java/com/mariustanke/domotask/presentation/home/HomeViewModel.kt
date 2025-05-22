package com.mariustanke.domotask.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mariustanke.domotask.domain.model.Board
import com.mariustanke.domotask.domain.model.Status
import com.mariustanke.domotask.domain.model.User
import com.mariustanke.domotask.domain.repository.AuthRepository
import com.mariustanke.domotask.domain.repository.UserRepository
import com.mariustanke.domotask.domain.uiModels.BoardUiModel
import com.mariustanke.domotask.domain.usecase.auth.UserUseCases
import com.mariustanke.domotask.domain.usecase.board.BoardUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userUseCases: UserUseCases,
    private val boardUseCases: BoardUseCases
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    val boards: StateFlow<List<BoardUiModel>> = boardUseCases.getBoards()
        .map { list ->
            val currentUserId = authRepository.getCurrentUser()?.uid
            val filtered = list.filter { it.members.contains(currentUserId) }

            filtered.map { board ->
                val name = userUseCases.getUser(board.createdBy)?.name ?: board.createdBy
                BoardUiModel(
                    id = board.id,
                    name = board.name,
                    description = board.description,
                    createdByName = name
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _invitations = MutableStateFlow<List<BoardUiModel>>(emptyList())
    val invitations: StateFlow<List<BoardUiModel>> = _invitations.asStateFlow()

    private val _acceptRejectState = MutableStateFlow<Result<Unit>?>(null)
    val acceptRejectState: StateFlow<Result<Unit>?> = _acceptRejectState.asStateFlow()

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUser()?.uid ?: return@launch
            userUseCases.getUser(uid)?.let { _user.value = it }
        }
    }

    fun createBoard(name: String, description: String) {
        val currentUser = authRepository.getCurrentUser() ?: return

        val defaultStatuses = listOf(
            Status(name = "To-Do", order = 1),
            Status(name = "In Progress", order = 2),
            Status(name = "Done", order = 3)
        )

        viewModelScope.launch {
            val boardId = boardUseCases.createBoard(
                Board(
                    name = name,
                    description = description,
                    createdBy = currentUser.uid,
                    members = listOf(currentUser.uid),
                    statuses = emptyList()
                )
            )

            defaultStatuses.forEach { status ->
                boardUseCases.createBoardStatus(boardId, status)
            }
        }
    }

    fun acceptInvitation(boardId: String) {
        val uid = authRepository.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _acceptRejectState.value = boardUseCases.acceptInvitationUseCase(boardId, uid)
        }
    }

    fun rejectInvitation(boardId: String) {
        val uid = authRepository.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _acceptRejectState.value = boardUseCases.rejectInvitationUseCase(boardId, uid)
        }
    }

    fun clearAcceptRejectState() {
        _acceptRejectState.value = null
    }
}
