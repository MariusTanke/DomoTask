package com.mariustanke.domotask.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    //onChangePassword: () -> Unit
) {
    val state by viewModel.profileState.collectAsState()

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (state) {
                is ProfileState.Loading -> {
                    CircularProgressIndicator()
                }

                is ProfileState.Success -> {
                    val profile = state as ProfileState.Success
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Imagen de perfil",
                            modifier = Modifier
                                .size(120.dp)
                                .padding(8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(profile.name, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            profile.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CounterItem(title = "Creados", count = 12)
                            CounterItem(title = "Resueltos", count = 7)
                            CounterItem(title = "Comentarios", count = 31)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Cambiar contraseña",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                //onChangePassword()
                            }
                        )
                    }
                }

                is ProfileState.Error -> {
                    Text(
                        text = (state as ProfileState.Error).message ?: "Error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun CounterItem(title: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$count", style = MaterialTheme.typography.headlineSmall)
        Text(text = title, style = MaterialTheme.typography.bodySmall)
    }
}
