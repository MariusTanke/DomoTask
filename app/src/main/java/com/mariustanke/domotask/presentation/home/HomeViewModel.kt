package com.mariustanke.domotask.presentation.home

import androidx.lifecycle.ViewModel
import com.mariustanke.domotask.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun logout() {
        authRepository.signOut()
    }
}
