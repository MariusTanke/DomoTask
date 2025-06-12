package com.mariustanke.domotask.domain.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mariustanke.domotask.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    suspend fun getUserById(uid: String): User? {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserFlow(uid: String): Flow<User?> = callbackFlow {
        val docRef = usersCollection.document(uid)
        val registration: ListenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val userObj = snapshot?.toObject(User::class.java)
            trySend(userObj).isSuccess
        }

        awaitClose {
            registration.remove()
        }
    }

    private suspend fun generateUniqueInvitationCode(): String {
        val charset = ('A'..'Z') + ('0'..'9')
        var code: String
        var exists: Boolean

        do {
            code = (1..8)
                .map { charset.random() }
                .joinToString("")

            val querySnapshot = firestore.collection("users")
                .whereEqualTo("invitationCode", code)
                .get()
                .await()

            exists = !querySnapshot.isEmpty
        } while (exists)

        return code
    }

    suspend fun saveUserToFirestore(user: User) {
        val userRef = usersCollection.document(user.id)
        val snapshot = userRef.get().await()

        if (!snapshot.exists()) {
            val invitationCode = generateUniqueInvitationCode()
            val newUser = user.copy(invitationCode = invitationCode)
            userRef.set(newUser).await()
        }
    }

    suspend fun updateFcmToken(uid: String, token: String) {
        try {
            usersCollection.document(uid).update("fcmToken", token).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateUser(user: User) {
        usersCollection
            .document(user.id)
            .set(user)
            .await()
    }

    suspend fun getUserByInvitationCode(code: String): User? {
        val snap = usersCollection.whereEqualTo("invitationCode", code).get().await()
        return snap.documents
            .firstOrNull()
            ?.toObject(User::class.java)
            ?.copy(id = snap.documents.first().id)
    }

    suspend fun addInvitation(userId: String, boardId: String) {
        val userRef = usersCollection.document(userId)
        firestore.runTransaction { tx ->
            val uSnap = tx.get(userRef)
            val current = uSnap.get("invitations") as? List<String> ?: emptyList()
            if (!current.contains(boardId)) {
                val updated = current + boardId
                tx.update(userRef, "invitations", updated)
            }
        }.await()
    }

    suspend fun removeInvitation(userId: String, boardId: String) {
        val userRef = usersCollection.document(userId)
        firestore.runTransaction { tx ->
            val uSnap = tx.get(userRef)
            val current = uSnap.get("invitations") as? List<String> ?: emptyList()
            if (current.contains(boardId)) {
                tx.update(userRef, "invitations", current - boardId)
            }
        }.await()
    }
}