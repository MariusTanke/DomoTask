package com.mariustanke.domotask.presentation.ticket

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.mariustanke.domotask.domain.model.Comment
import com.mariustanke.domotask.domain.model.Ticket
import com.mariustanke.domotask.domain.model.User
import com.mariustanke.domotask.domain.usecase.auth.UserUseCases
import com.mariustanke.domotask.domain.usecase.board.BoardUseCases
import com.mariustanke.domotask.domain.usecase.comment.CommentUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class TicketViewModel @Inject constructor(
    application: Application,
    private val boardUseCases: BoardUseCases,
    private val commentUseCases: CommentUseCases,
    private val userUseCases: UserUseCases,
    private val firebaseAuth: FirebaseAuth,
    private val storage: FirebaseStorage,
) : ViewModel() {

    private val appContext = application.applicationContext

    private val _ticket = MutableStateFlow<Ticket?>(null)
    val ticket: StateFlow<Ticket?> = _ticket

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _members = MutableStateFlow<List<User>>(emptyList())
    val members: StateFlow<List<User>> = _members

    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    fun loadTicketAndComments(boardId: String, ticketId: String) {
        viewModelScope.launch {
            boardUseCases.getTickets(boardId)
                .first()
                .find { it.id == ticketId }
                ?.let { _ticket.value = it }

            commentUseCases.getComments(boardId, ticketId)
                .collect { list -> _comments.value = list }
        }
    }

    fun loadBoardMembers(boardId: String) {
        viewModelScope.launch {
            val board = boardUseCases.getBoard(boardId).first()
            val membersList = coroutineScope {
                board.members.map { uid -> async { userUseCases.getUser(uid) } }
            }
            _members.value = membersList.awaitAll().filterNotNull()
        }
    }

    fun updateTicket(boardId: String, updated: Ticket) {
        viewModelScope.launch {
            boardUseCases.updateTicket(boardId, updated)
        }
    }

    fun sendComment(
        boardId: String,
        ticketId: String,
        text: String,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            if (imageUri != null) {
                val inputStream = appContext.contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val baos = ByteArrayOutputStream().apply {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, this)
                }

                val data = baos.toByteArray()

                val ref = storage.reference
                    .child("comments/$boardId/$ticketId/${System.currentTimeMillis()}")

                ref.putBytes(data).await()
                val url = ref.downloadUrl.await().toString()
                val comment = Comment(
                    content = text,
                    imageUrl = url,
                    createdBy = currentUser?.uid.orEmpty(),
                    createdAt = System.currentTimeMillis()
                )
                commentUseCases.addComment(boardId, ticketId, comment)
            } else {
                val comment = Comment(
                    content = text,
                    createdBy = currentUser?.uid.orEmpty(),
                    createdAt = System.currentTimeMillis()
                )
                commentUseCases.addComment(boardId, ticketId, comment)
            }
        }
    }

    fun updateComment(boardId: String, ticketId: String, comment: Comment) {
        viewModelScope.launch {
            commentUseCases.updateComment(boardId, ticketId, comment)
        }
    }

    fun deleteComment(boardId: String, ticketId: String, comment: Comment) {
        viewModelScope.launch {
            comment.imageUrl?.takeIf { it.isNotBlank() }?.let { url ->
                try {
                    storage.getReferenceFromUrl(url).delete().await()
                } catch (e: Exception) {
                    Log.e("Storage", "Error deleting image: ${e.message}")
                }
            }
            commentUseCases.deleteComment(boardId, ticketId, comment.id)
        }
    }

    fun getUserName(userId: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val user = userUseCases.getUser(userId)
            onResult(user?.name ?: "Desconocido")
        }
    }
}