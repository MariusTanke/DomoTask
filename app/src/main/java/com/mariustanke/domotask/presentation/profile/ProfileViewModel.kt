package com.mariustanke.domotask.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mariustanke.domotask.domain.model.User
import com.mariustanke.domotask.domain.repository.AuthRepository
import com.mariustanke.domotask.domain.usecase.auth.UserUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userUseCases: UserUseCases
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        val userAuth = authRepository.getCurrentUser()
        if (userAuth != null) {
            viewModelScope.launch {
                val userDB = userUseCases.getUser(userAuth.uid)
                if (userDB != null) {
                    _profileState.value = ProfileState.Success(userDB)
                } else {
                    _profileState.value = ProfileState.Error("Usuario no encontrado en la base de datos")
                }
            }
        } else {
            _profileState.value = ProfileState.Error("Usuario no autenticado")
        }
    }

    fun sendPasswordReset() {
        val email = authRepository.getCurrentUser()?.email
        if (!email.isNullOrBlank()) {
            // FirebaseAuth.getInstance().sendPasswordResetEmail(email)
        }
    }
}

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val user: User) : ProfileState()
    data class Error(val message: String?) : ProfileState()
}