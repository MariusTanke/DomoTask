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
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userUseCases: UserUseCases,
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginState = _loginState.asStateFlow()

    fun loginWithEmail(email: String, password: String) {
        _loginState.value = LoginResult.Loading
        authRepository.signInWithEmail(email, password) { success, error ->
            if (success) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        viewModelScope.launch {
                            try {
                                userUseCases.updateFcmToken(user.uid, token)
                            } catch (e: Exception) {
                                Log.e("FCM", "Error guardando token: ${e.message}")
                            }
                        }
                    }
                }
                _loginState.value = LoginResult.Success
            } else {
                _loginState.value = LoginResult.Error(error)
            }
        }
    }

    private fun loginWithGoogle(credential: AuthCredential) {
        _loginState.value = LoginResult.Loading
        authRepository.signInWithGoogle(credential) { success, error, user ->
            if (success && user != null) {
                viewModelScope.launch {
                    try {
                        userUseCases.createUser(user)
                        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                            viewModelScope.launch {
                                try {
                                    userUseCases.updateFcmToken(user.id, token)
                                } catch (e: Exception) {
                                    Log.e("FCM", "Error guardando token: ${e.message}")
                                }
                            }
                        }

                        _loginState.value = LoginResult.Success
                    } catch (e: Exception) {
                        _loginState.value = LoginResult.Error("Error al guardar el usuario: ${e.message}")
                    }
                }
            } else {
                _loginState.value = LoginResult.Error(error)
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