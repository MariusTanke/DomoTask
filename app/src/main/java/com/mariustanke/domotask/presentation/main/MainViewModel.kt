package com.mariustanke.domotask.presentation.main

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.mariustanke.domotask.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val authRepository: AuthRepository
) : ViewModel() {

    fun signOut() {
        firebaseAuth.signOut()
    }
}