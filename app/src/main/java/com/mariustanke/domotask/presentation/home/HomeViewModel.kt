package com.mariustanke.domotask.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mariustanke.domotask.domain.model.Board
import com.mariustanke.domotask.domain.model.Status
import com.mariustanke.domotask.domain.model.User
import com.mariustanke.domotask.domain.repository.AuthRepository
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

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        val uid = authRepository.getCurrentUser()?.uid ?: return
        userUseCases.getUserFlow(uid)
            .onEach { fetchedUser ->
                _user.value = fetchedUser
            }
            .launchIn(viewModelScope)
    }

    fun loadUser() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUser()?.uid ?: return@launch
            userUseCases.getUser(uid)?.let { u ->
                _user.value = u
            }
        }
    }

    val boards: StateFlow<List<BoardUiModel>> = boardUseCases.getBoards()
        .combine(user) { boardList, user ->
            val currentUid = user?.id
            boardList.filter { it.members.contains(currentUid) }
        }
        .map { list ->
            list.map { board ->
                val creatorName = userUseCases.getUser(board.createdBy)?.name ?: board.createdBy
                BoardUiModel(
                    id = board.id,
                    name = board.name,
                    description = board.description,
                    createdByName = creatorName
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invitations: StateFlow<List<BoardUiModel>> = boardUseCases.getBoards()
        .combine(user) { boardList, user ->
            val invites = user?.invitations.orEmpty()
            boardList.filter { invites.contains(it.id) }
        }
        .map { list ->
            list.map { board ->
                val creatorName = userUseCases.getUser(board.createdBy)?.name ?: board.createdBy
                BoardUiModel(
                    id = board.id,
                    name = board.name,
                    description = board.description,
                    createdByName = creatorName
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _acceptRejectState = MutableStateFlow<Result<Unit>?>(null)
    val acceptRejectState: StateFlow<Result<Unit>?> = _acceptRejectState.asStateFlow()

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
}