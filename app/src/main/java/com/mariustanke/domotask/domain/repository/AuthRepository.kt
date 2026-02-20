package com.mariustanke.domotask.domain.repository

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.mariustanke.domotask.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient,
) {

    fun getGoogleSignInIntent(): Intent {
        googleSignInClient.signOut()
        return googleSignInClient.signInIntent
    }

    fun extractGoogleCredentialFromIntent(data: Intent?): AuthCredential? {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            GoogleAuthProvider.getCredential(account.idToken, null)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signInWithEmail(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signInWithGoogle(credential: AuthCredential): User {
        firebaseAuth.signInWithCredential(credential).await()
        val user = getCurrentUser()
            ?: throw IllegalStateException("Usuario nulo tras login")
        return User(
            id = user.uid,
            name = user.displayName ?: "",
            email = user.email ?: ""
        )
    }

    suspend fun registerWithEmail(
        name: String,
        email: String,
        password: String,
    ): User {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = getCurrentUser()
            ?: throw IllegalStateException("Usuario nulo tras registro")
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(profileUpdates).await()
        return User(
            id = user.uid,
            name = name,
            email = email
        )
    }

    fun getCurrentUser() = firebaseAuth.currentUser

    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null
}
