package com.mariustanke.domotask.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mariustanke.domotask.R
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.profileState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onProfileImageSelected(it) }
    }

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

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
                    val profileState = state as ProfileState.Success
                    val profile = profileState.user

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        if (profile.photo != null) {
                            AsyncImage(
                                model = profile.photo,
                                contentDescription = "Nueva foto de perfil",
                                modifier = Modifier
                                    .size(320.dp)
                                    .clip(CircleShape),
                                placeholder = painterResource(R.drawable.placeholder_avatar),
                                error = painterResource(R.drawable.placeholder_avatar),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Imagen de perfil",
                                modifier = Modifier
                                    .size(320.dp)
                                    .padding(8.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(profile.name, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            profile.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(24.dp))

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
                                Text(
                                    text = "#${profile.invitationCode}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.Center)
                                )

                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(profile.invitationCode))
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

                        Spacer(Modifier.height(32.dp))

                        Text(
                            text = "Cambiar contraseña",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                viewModel.sendPasswordReset()
                            }
                        )

                        Spacer(Modifier.height(32.dp))

                        Text(
                            text = "Cambiar imagen de perfil",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                galleryLauncher.launch("image/*")
                            }
                        )

                        SnackbarHost(hostState = snackbarHostState)
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