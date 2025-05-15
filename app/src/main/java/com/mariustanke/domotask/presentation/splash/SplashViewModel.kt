package com.mariustanke.domotask.presentation.splash

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mariustanke.domotask.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val isUserLoggedIn = mutableStateOf<Boolean?>(null)

    init {
        viewModelScope.launch {
            delay(2000)
            isUserLoggedIn.value = authRepository.isUserLoggedIn()
        }
    }
}
