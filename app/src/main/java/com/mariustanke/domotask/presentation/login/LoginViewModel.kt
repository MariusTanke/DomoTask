package com.mariustanke.domotask.presentation.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.mariustanke.domotask.domain.repository.AuthRepository
import com.mariustanke.domotask.domain.usecase.auth.UserUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userUseCases: UserUseCases,
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginState = _loginState.asStateFlow()

    private val _resetState = MutableStateFlow<ResetResult>(ResetResult.Idle)
    val resetState = _resetState.asStateFlow()

    fun resetLoginState() {
        _loginState.value = LoginResult.Idle
    }

    fun loginWithEmail(email: String, password: String) {
        _loginState.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                authRepository.signInWithEmail(email, password)
                updateFcmToken()
                _loginState.value = LoginResult.Success
            } catch (e: Exception) {
                _loginState.value = LoginResult.Error(e.localizedMessage)
            }
        }
    }

    private fun loginWithGoogle(credential: AuthCredential) {
        _loginState.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                val user = authRepository.signInWithGoogle(credential)
                userUseCases.createUser(user)
                updateFcmToken()
                _loginState.value = LoginResult.Success
            } catch (e: Exception) {
                _loginState.value = LoginResult.Error(e.localizedMessage)
            }
        }
    }

    private suspend fun updateFcmToken() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            val uid = authRepository.getCurrentUser()?.uid ?: return
            userUseCases.updateFcmToken(uid, token)
        } catch (e: Exception) {
            Log.e("FCM", "Error guardando token: ${e.message}")
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _resetState.value = ResetResult.Error("El correo no puede estar vacío")
            return
        }

        _resetState.value = ResetResult.Loading
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
                _resetState.value = ResetResult.Success
            } catch (e: Exception) {
                _resetState.value = ResetResult.Error(
                    e.localizedMessage ?: "Error al enviar correo"
                )
            }
        }
    }

    fun getGoogleSignInIntent(): android.content.Intent {
        return authRepository.getGoogleSignInIntent()
    }

    fun handleGoogleSignInResult(data: android.content.Intent?) {
        val credential = authRepository.extractGoogleCredentialFromIntent(data)
        credential?.let {
            loginWithGoogle(it)
        } ?: run {
            _loginState.value = LoginResult.Error("No se pudo obtener las credenciales de Google")
        }
    }
}

sealed class LoginResult {
    data object Idle : LoginResult()
    data object Loading : LoginResult()
    data object Success : LoginResult()
    data class Error(val message: String?) : LoginResult()
}

sealed class ResetResult {
    data object Idle : ResetResult()
    data object Loading : ResetResult()
    data object Success : ResetResult()
    data class Error(val message: String) : ResetResult()
}
