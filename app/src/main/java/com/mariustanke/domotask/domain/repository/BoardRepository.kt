package com.mariustanke.domotask.domain.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mariustanke.domotask.domain.model.Board
import com.mariustanke.domotask.domain.model.Status
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
    private fun ticketsCollection(boardId: String): CollectionReference =
        boardsCollection.document(boardId).collection("tickets")
    private fun statusesCollection(boardId: String): CollectionReference =
        boardsCollection.document(boardId).collection("statuses")

    fun getBoards(): Flow<List<Board>> = callbackFlow {
        val listener = boardsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val boards = snapshot?.documents
                ?.mapNotNull { it.toObject(Board::class.java)?.copy(id = it.id) }
                ?: emptyList()
            trySend(boards)
        }
        awaitClose { listener.remove() }
    }

    fun getBoard(boardId: String): Flow<Board> = callbackFlow {
        val listener = boardsCollection.document(boardId)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                snap?.toObject(Board::class.java)
                    ?.copy(id = snap.id)
                    ?.let { trySend(it) }
            }
        awaitClose { listener.remove() }
    }

    suspend fun createBoard(board: Board): String {
        val docRef = boardsCollection.document()
        val boardWithId = board.copy(id = docRef.id)
        docRef.set(boardWithId).await()
        return docRef.id
    }

    suspend fun updateBoard(board: Board) {
        boardsCollection
            .document(board.id)
            .set(board)
            .await()
    }

    suspend fun deleteBoard(boardId: String) {
        boardsCollection
            .document(boardId)
            .delete()
            .await()
    }

    suspend fun addMemberToBoard(boardId: String, userId: String) {
        val boardRef = boardsCollection.document(boardId)
        firestore.runTransaction { tx ->
            val bSnap = tx.get(boardRef)
            val current = bSnap.get("members") as? List<String> ?: emptyList()
            if (!current.contains(userId)) {
                tx.update(boardRef, "members", current + userId)
            }
        }.await()
    }

    suspend fun removeMemberFromBoard(boardId: String, userId: String) {
        boardsCollection
            .document(boardId)
            .update("members", FieldValue.arrayRemove(userId))
            .await()
    }

    /** ─── Tickets ───────────────────────────────────────────────────────── */

    fun getTickets(boardId: String): Flow<List<Ticket>> = callbackFlow {
        val ref = ticketsCollection(boardId)
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val tickets = snapshot?.documents
                ?.mapNotNull { it.toObject(Ticket::class.java)?.copy(id = it.id) }
                ?: emptyList()
            trySend(tickets)
        }

        awaitClose { listener.remove() }
    }

    fun getTicket(
        boardId: String,
        ticketId: String
    ): Flow<Ticket> = callbackFlow {
        val ticketRef = ticketsCollection(boardId).document(ticketId)

        var baseTicket: Ticket? = null
        var childTickets: List<Ticket> = emptyList()

        val ticketListener = ticketRef.addSnapshotListener { snap, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            snap?.toObject(Ticket::class.java)
                ?.copy(id = snap.id)
                ?.also { ticket ->
                    baseTicket = ticket
                    trySend(ticket.copy(subTickets = childTickets.toMutableList()))
                }
        }

        val childrenQuery = ticketsCollection(boardId)
            .whereEqualTo("parentId", ticketId)
        val childrenListener = childrenQuery.addSnapshotListener { snap, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            childTickets = snap?.documents
                ?.mapNotNull { it.toObject(Ticket::class.java)?.copy(id = it.id) }
                ?: emptyList()

            baseTicket?.let { base ->
                trySend(base.copy(subTickets = childTickets.toMutableList()))
            }
        }

        awaitClose {
            ticketListener.remove()
            childrenListener.remove()
        }
    }

    suspend fun addTicket(boardId: String, ticket: Ticket): String {
        val ref = ticketsCollection(boardId).document()
        val ticketWithId = ticket.copy(id = ref.id)
        ref.set(ticketWithId).await()

        return ref.id
    }

    suspend fun addSubTicket(
        boardId: String,
        ticketId: String,
        subTicket: Ticket
    ): String {
        val ref = ticketsCollection(boardId)
            .document(ticketId)
            .collection("subtickets")
            .document()
        val withId = subTicket.copy(id = ref.id)
        ref.set(withId).await()

        return ref.id
    }

    suspend fun updateTicket(boardId: String, ticket: Ticket) {
        ticketsCollection(boardId)
            .document(ticket.id)
            .set(ticket)
            .await()
    }


    suspend fun deleteTicket(boardId: String, ticketId: String) {
        ticketsCollection(boardId)
            .document(ticketId)
            .delete()
            .await()
    }

    /** ─── Comments ──────────────────────────────────────────────────────── */

    suspend fun addComment(
        boardId: String,
        ticketId: String,
        comment: Comment
    ): String {
        val ref = ticketsCollection(boardId)
            .document(ticketId)
            .collection("comments")
            .document()
        val withId = comment.copy(id = ref.id)
        ref.set(withId).await()
        return ref.id
    }

    fun getComments(boardId: String, ticketId: String): Flow<List<Comment>> = callbackFlow {
        val ref = ticketsCollection(boardId)
            .document(ticketId)
            .collection("comments")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val comments = snapshot?.documents
                ?.mapNotNull { it.toObject(Comment::class.java)?.copy(id = it.id) }
                ?: emptyList()
            trySend(comments)
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateComment(
        boardId: String,
        ticketId: String,
        comment: Comment
    ) {
        ticketsCollection(boardId)
            .document(ticketId)
            .collection("comments")
            .document(comment.id)
            .set(comment)
            .await()
    }

    suspend fun deleteComment(
        boardId: String,
        ticketId: String,
        commentId: String
    ) {
        ticketsCollection(boardId)
            .document(ticketId)
            .collection("comments")
            .document(commentId)
            .delete()
            .await()
    }

    /** ─── Statuses ──────────────────────────────────────────────────────── */

    fun getStatuses(boardId: String): Flow<List<Status>> = callbackFlow {
        val listener = statusesCollection(boardId)
            .orderBy("order")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents
                    ?.mapNotNull { it.toObject(Status::class.java)?.copy(id = it.id) }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createStatus(boardId: String, status: Status): String {
        val ref = statusesCollection(boardId).document(status.id)
        ref.set(status).await()
        return status.id
    }

    suspend fun updateStatus(boardId: String, status: Status) {
        statusesCollection(boardId)
            .document(status.id)
            .set(status)
            .await()
    }

    suspend fun deleteStatus(boardId: String, statusId: String) {
        statusesCollection(boardId)
            .document(statusId)
            .delete()
            .await()
    }
}
