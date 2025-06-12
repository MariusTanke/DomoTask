package com.mariustanke.domotask.domain.repository

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.mariustanke.domotask.domain.model.User
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient,
    private val firestore: FirebaseFirestore
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

    fun signInWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun signInWithGoogle(
        credential: AuthCredential,
        onResult: (Boolean, String?, User?) -> Unit
    ) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = getCurrentUser()
                    if (user != null) {
                        val newUser = User(
                            id = user.uid,
                            name = user.displayName ?: "",
                            email = user.email ?: ""
                        )
                        onResult(true, null, newUser)
                    } else {
                        onResult(false, "Usuario nulo tras login", null)
                    }
                } else {
                    onResult(false, task.exception?.message, null)
                }
            }
    }

    fun registerWithEmail(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String?, User?) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = getCurrentUser()
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                        val newUser = User(
                            id = user.uid,
                            name = name,
                            email = email
                        )
                        onResult(true, null, newUser)
                    } ?: onResult(false, "No se pudo actualizar el perfil", null)
                } else {
                    onResult(false, task.exception?.message, null)
                }
            }
    }

    fun getCurrentUser() = firebaseAuth.currentUser

    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null
}