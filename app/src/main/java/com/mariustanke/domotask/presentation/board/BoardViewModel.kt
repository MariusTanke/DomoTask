package com.mariustanke.domotask.presentation.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mariustanke.domotask.domain.model.Ticket
import com.mariustanke.domotask.domain.usecase.board.BoardUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _tickets = MutableStateFlow<List<Ticket>>(emptyList())
    val tickets: StateFlow<List<Ticket>> = _tickets.asStateFlow()

    val currentUser : FirebaseUser? get() = firebaseAuth.currentUser

    fun loadTickets(boardId: String) {
        viewModelScope.launch {
            boardUseCases.getTickets(boardId).collect {
                _tickets.value = it
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
}
