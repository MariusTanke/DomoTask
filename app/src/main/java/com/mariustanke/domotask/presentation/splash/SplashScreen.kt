package com.mariustanke.domotask.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mariustanke.domotask.R
import com.mariustanke.domotask.ui.theme.appGradientBackground

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onLogin: () -> Unit,
    onHome: () -> Unit
) {
    val isLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        when (isLoggedIn) {
            true -> onHome()
            false -> onLogin()
            null -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appGradientBackground()),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.dometask_logo_v2),
                contentDescription = "DomoTask Logo",
                modifier = Modifier.size(360.dp)
            )
        }
    }
}
