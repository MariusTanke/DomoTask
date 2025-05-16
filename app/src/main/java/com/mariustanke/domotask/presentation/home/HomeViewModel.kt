package com.mariustanke.domotask.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mariustanke.domotask.domain.model.Board
import com.mariustanke.domotask.domain.repository.AuthRepository
import com.mariustanke.domotask.domain.uiModels.BoardUiModel
import com.mariustanke.domotask.domain.usecase.board.BoardUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val boardUseCases: BoardUseCases
) : ViewModel() {

    val boards: StateFlow<List<BoardUiModel>> = boardUseCases.getBoards()
        .map { list ->
            val currentUserId = authRepository.getCurrentUser()?.uid
            val filtered = list.filter { it.members.contains(currentUserId) }

            filtered.map { board ->
                val name = authRepository.getUserById(board.createdBy)?.name ?: board.createdBy
                BoardUiModel(
                    id = board.id,
                    name = board.name,
                    description = board.description,
                    createdByName = name
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createBoard(name: String, description: String) {
        val currentUser = authRepository.getCurrentUser() ?: return
        val board = Board(
            name = name,
            description = description,
            createdBy = currentUser.uid,
            members = listOf(currentUser.uid)
        )

        viewModelScope.launch {
            boardUseCases.createBoard(board)
        }
    }
}
