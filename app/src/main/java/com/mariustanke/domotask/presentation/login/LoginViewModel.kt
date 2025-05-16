package com.mariustanke.domotask.presentation.login

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.mariustanke.domotask.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginState = _loginState.asStateFlow()

    fun loginWithEmail(email: String, password: String) {
        _loginState.value = LoginResult.Loading
        authRepository.signInWithEmail(email, password) { success, error ->
            _loginState.value = if (success) LoginResult.Success else LoginResult.Error(error)
        }
    }

    private fun loginWithGoogle(credential: AuthCredential) {
        _loginState.value = LoginResult.Loading
        authRepository.signInWithGoogle(credential) { success, error ->
            _loginState.value = if (success) LoginResult.Success else LoginResult.Error(error)
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
