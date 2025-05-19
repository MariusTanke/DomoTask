package com.mariustanke.domotask.presentation.ticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mariustanke.domotask.domain.model.Comment
import com.mariustanke.domotask.domain.model.Ticket
import com.mariustanke.domotask.domain.usecase.auth.UserUseCases
import com.mariustanke.domotask.domain.usecase.board.BoardUseCases
import com.mariustanke.domotask.domain.usecase.comment.CommentUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val commentUseCases: CommentUseCases,
    private val userUseCases: UserUseCases,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _ticket = MutableStateFlow<Ticket?>(null)
    val ticket: StateFlow<Ticket?> = _ticket

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    fun loadTicketAndComments(boardId: String, ticketId: String) {
        viewModelScope.launch {
            boardUseCases.getTickets(boardId).first().find { it.id == ticketId }?.let {
                _ticket.value = it
            }

            commentUseCases.getComments(boardId, ticketId).collect { list ->
                _comments.value = list
            }
        }
    }

    fun updateTicket(boardId: String, updated: Ticket) {
        viewModelScope.launch {
            boardUseCases.updateTicket(boardId, updated)
        }
    }

    fun addComment(boardId: String, ticketId: String, comment: Comment) {
        viewModelScope.launch {
            commentUseCases.addComment(boardId, ticketId, comment)
        }
    }

    fun updateComment(boardId: String, ticketId: String, comment: Comment) {
        viewModelScope.launch {
            commentUseCases.updateComment(boardId, ticketId, comment)
        }
    }

    fun deleteComment(boardId: String, ticketId: String, commentId: String) {
        viewModelScope.launch {
            commentUseCases.deleteComment(boardId, ticketId, commentId)
        }
    }

    fun getUser(userId: String) {
        viewModelScope.launch {
            userUseCases.getUser(userId)
        }
    }

    fun getUserName(userId: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val user = userUseCases.getUser(userId)
            onResult(user?.name ?: "Desconocido")
        }
    }
}
