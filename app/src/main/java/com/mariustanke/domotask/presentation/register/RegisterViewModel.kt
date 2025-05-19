package com.mariustanke.domotask.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mariustanke.domotask.domain.repository.AuthRepository
import com.mariustanke.domotask.domain.usecase.auth.UserUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userUseCases: UserUseCases
) : ViewModel() {


    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState = _registerState.asStateFlow()

    fun register(name: String, email: String, password: String) {
        _registerState.value = RegisterState.Loading
        authRepository.registerWithEmail(name, email, password) { success, error, user ->
            if (success && user != null) {
                viewModelScope.launch {
                    try {
                        userUseCases.createUser(user)
                        _registerState.value = RegisterState.Success
                    } catch (e: Exception) {
                        _registerState.value = RegisterState.Error("Error al guardar el usuario: ${e.message}")
                    }
                }
            } else {
                _registerState.value = RegisterState.Error(error)
            }
        }
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String?) : RegisterState()
}
