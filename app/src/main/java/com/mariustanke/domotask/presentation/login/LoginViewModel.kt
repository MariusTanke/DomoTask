package com.mariustanke.domotask.presentation.login

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.mariustanke.domotask.data.repository.AuthRepository
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

    fun loginWithGoogle(credential: AuthCredential) {
        _loginState.value = LoginResult.Loading
        Log.d("DEBUG", "Iniciando login con Google")
        authRepository.signInWithGoogle(credential) { success, error ->
            Log.d("DEBUG", "Resultado login Google: success=$success, error=$error")
            _loginState.value = if (success) LoginResult.Success else LoginResult.Error(error)
        }
    }

}

sealed class LoginResult {
    data object Idle : LoginResult()
    data object Loading : LoginResult()
    data object Success : LoginResult()
    data class Error(val message: String?) : LoginResult()
}
