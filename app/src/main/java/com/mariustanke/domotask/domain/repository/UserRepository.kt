package com.mariustanke.domotask.domain.repository

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mariustanke.domotask.domain.model.User
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

    suspend fun generateUniqueInvitationCode(): String {
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
}