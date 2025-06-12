package com.mariustanke.domotask.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mariustanke.domotask.R

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
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Splash Icon",
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.Center)
        )
    }
}
