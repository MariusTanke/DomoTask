package com.mariustanke.domotask.domain.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mariustanke.domotask.domain.model.Board
import com.mariustanke.domotask.domain.model.Ticket
import com.mariustanke.domotask.domain.model.Comment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BoardRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val boardsCollection = firestore.collection("boards")

    fun getBoards(): Flow<List<Board>> = callbackFlow {
        val listener = boardsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val boards = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Board::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            trySend(boards)
        }

        awaitClose { listener.remove() }
    }

    suspend fun createBoard(board: Board): String {
        val docRef = boardsCollection.document()
        val boardWithId = board.copy(id = docRef.id)
        docRef.set(boardWithId).await()
        return docRef.id
    }

    fun getTickets(boardId: String): Flow<List<Ticket>> = callbackFlow {
        val ref = boardsCollection.document(boardId).collection("tickets")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val tickets = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Ticket::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            trySend(tickets)
        }

        awaitClose { listener.remove() }
    }

    suspend fun updateTicket(boardId: String, ticket: Ticket) {
        val ref = boardsCollection
            .document(boardId)
            .collection("tickets")
            .document(ticket.id)
        ref.set(ticket).await()
    }

    suspend fun deleteTicket(boardId: String, ticketId: String) {
        val ref = boardsCollection
            .document(boardId)
            .collection("tickets")
            .document(ticketId)
        ref.delete().await()
    }

    suspend fun addTicket(boardId: String, ticket: Ticket): String {
        val ref = boardsCollection.document(boardId).collection("tickets").document()
        val ticketWithId = ticket.copy(id = ref.id)
        ref.set(ticketWithId).await()
        return ref.id
    }

    suspend fun addSubticket(boardId: String, ticketId: String, subticket: Ticket): String {
        val ref = boardsCollection
            .document(boardId)
            .collection("tickets")
            .document(ticketId)
            .collection("subtickets")
            .document()

        val withId = subticket.copy(id = ref.id)
        ref.set(withId).await()
        return ref.id
    }

    suspend fun addComment(boardId: String, ticketId: String, comment: Comment): String {
        val ref = boardsCollection
            .document(boardId)
            .collection("tickets")
            .document(ticketId)
            .collection("comments")
            .document()

        val withId = comment.copy(id = ref.id)
        ref.set(withId).await()
        return ref.id
    }

    fun getComments(boardId: String, ticketId: String): Flow<List<Comment>> = callbackFlow {
        val ref = boardsCollection
            .document(boardId)
            .collection("tickets")
            .document(ticketId)
            .collection("comments")

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val comments = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Comment::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            trySend(comments)
        }

        awaitClose { listener.remove() }
    }

    suspend fun updateComment(boardId: String, ticketId: String, comment: Comment) {
        val ref = boardsCollection
            .document(boardId)
            .collection("tickets")
            .document(ticketId)
            .collection("comments")
            .document(comment.id)

        ref.set(comment).await()
    }

    suspend fun deleteComment(boardId: String, ticketId: String, commentId: String) {
        val ref = boardsCollection
            .document(boardId)
            .collection("tickets")
            .document(ticketId)
            .collection("comments")
            .document(commentId)

        ref.delete().await()
    }
}