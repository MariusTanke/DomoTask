package com.mariustanke.domotask.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

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
                    val user = state as ProfileState.Success
                    val profile = user.user
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Imagen de perfil",
                                modifier = Modifier
                                    .size(240.dp)
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

                            val snackbarHostState = remember { SnackbarHostState() }
                            val clipboardManager = LocalClipboardManager.current
                            val coroutineScope = rememberCoroutineScope()

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Código de invitación",
                                    style = MaterialTheme.typography.titleMedium,
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Texto centrado
                                    Text(
                                        text = "#${profile.invitationCode}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.align(Alignment.Center)
                                    )

                                    // Ícono posicionado justo al lado derecho del texto, sin desplazar el texto
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(profile.invitationCode))
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Código copiado")
                                            }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .offset(x = (profile.invitationCode.length * 6).dp + 12.dp) // Ajusta esta expresión según tipografía y tamaño
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Copiar código",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                text = "Cambiar contraseña",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    // onChangePassword()
                                }
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                text = "Cambiar imagen de perfil",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    // onChangePassword()
                                }
                            )
                        }
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

