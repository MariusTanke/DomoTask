package com.mariustanke.domotask.presentation.profile

import androidx.lifecycle.ViewModel
import com.mariustanke.domotask.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        val user = authRepository.getCurrentUser()
        if (user != null) {
            _profileState.value = ProfileState.Success(
                name = user.displayName ?: "Nombre no disponible",
                email = user.email ?: "Correo no disponible"
            )
        } else {
            _profileState.value = ProfileState.Error("Usuario no autenticado")
        }
    }

    fun sendPasswordReset() {
        val email = authRepository.getCurrentUser()?.email
        if (!email.isNullOrBlank()) {
            // Aquí podrías enviar el email si deseas
            // FirebaseAuth.getInstance().sendPasswordResetEmail(email)
        }
    }
}

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val name: String, val email: String) : ProfileState()
    data class Error(val message: String?) : ProfileState()
}