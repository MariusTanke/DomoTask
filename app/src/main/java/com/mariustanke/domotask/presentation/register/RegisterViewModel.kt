package com.mariustanke.domotask.presentation.register

import androidx.lifecycle.ViewModel
import com.mariustanke.domotask.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState = _registerState.asStateFlow()

    fun register(name: String, email: String, password: String) {
        _registerState.value = RegisterState.Loading
        authRepository.registerWithEmail(name, email, password) { success, error ->
            _registerState.value = if (success) RegisterState.Success
            else RegisterState.Error(error)
        }
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String?) : RegisterState()
}
