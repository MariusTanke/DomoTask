package com.mariustanke.domotask.presentation.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.mariustanke.domotask.domain.model.User
import com.mariustanke.domotask.domain.repository.AuthRepository
import com.mariustanke.domotask.domain.usecase.auth.UserUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val userUseCases: UserUseCases,
    private val storage: FirebaseStorage,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage

    private var currentUser: User? = null

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val authUser = authRepository.getCurrentUser()
            if (authUser == null) {
                _profileState.value = ProfileState.Error("Usuario no autenticado")
                return@launch
            }
            val user = userUseCases.getUser(authUser.uid)
            if (user == null) {
                _profileState.value = ProfileState.Error("Usuario no encontrado")
            } else {
                currentUser = user
                _profileState.value = ProfileState.Success(user)
            }
        }
    }

    fun onProfileImageSelected(uri: Uri) = uploadNewProfileImage(uri)

    private fun uploadNewProfileImage(uri: Uri) {
        val user = currentUser ?: return
        viewModelScope.launch {
            user.photo?.takeIf { it.isNotBlank() }?.let { oldUrl ->
                runCatching { storage.getReferenceFromUrl(oldUrl).delete().await() }
            }
            val ref = storage.reference
                .child("profiles/${user.id}/${System.currentTimeMillis()}")
            ref.putFile(uri).await()
            val newUrl = ref.downloadUrl.await().toString()

            val updatedUser = user.copy(photo = newUrl)
            userUseCases.updateUser(updatedUser)
            currentUser = updatedUser
            _profileState.value = ProfileState.Success(updatedUser)
        }
    }

    fun sendPasswordReset() {
        val email = authRepository.getCurrentUser()?.email
        if (email.isNullOrBlank()) return
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    if (task.isSuccessful) {
                        _snackbarMessage.emit("Email de restablecimiento enviado")
                    } else {
                        _snackbarMessage.emit(task.exception?.message ?: "Error al enviar email")
                    }
                }
            }
    }
}

sealed class ProfileState {
    data object Loading : ProfileState()
    data class Success(val user: User) : ProfileState()
    data class Error(val message: String?) : ProfileState()
}