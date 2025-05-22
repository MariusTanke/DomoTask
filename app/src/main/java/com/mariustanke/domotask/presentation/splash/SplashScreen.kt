package com.mariustanke.domotask.presentation.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onLogin: () -> Unit,
    onHome: () -> Unit
) {
    val isLoggedIn = viewModel.isUserLoggedIn.value

    LaunchedEffect(isLoggedIn) {
        when (isLoggedIn) {
            true -> onHome()
            false -> onLogin()
            null -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("DOMES Task !!", style = MaterialTheme.typography.headlineLarge)
    }
}
