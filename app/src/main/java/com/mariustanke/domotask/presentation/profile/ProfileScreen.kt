package com.mariustanke.domotask.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mariustanke.domotask.R
import com.mariustanke.domotask.presentation.board.initials
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

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

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
                        if (!profile.photo.isNullOrEmpty()) {
                            AsyncImage(
                                model = profile.photo,
                                contentDescription = stringResource(R.string.cd_new_profile_pic),
                                modifier = Modifier
                                    .size(300.dp)
                                    .clip(CircleShape),
                                placeholder = painterResource(R.drawable.placeholder_avatar),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(300.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials(profile.name),
                                    fontSize = 62.sp,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
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
                                text = stringResource(R.string.invitation_code_label),
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
                                        .offset(x = (profile.invitationCode.length * 6).dp + 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = stringResource(R.string.cd_copy_code),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(25.dp))

                        Text(
                            text = stringResource(R.string.change_password),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                showConfirmDialog = true
                            }
                        )

                        Spacer(Modifier.height(25.dp))

                        Text(
                            text = stringResource(R.string.change_profile_image),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                galleryLauncher.launch("image/*")
                            }
                        )

                        Spacer(Modifier.height(25.dp))

                        Text(
                            text = "Consultar políticas de uso",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                showTermsDialog = true
                            }
                        )

                        Spacer(Modifier.height(32.dp))

                        SnackbarHost(hostState = snackbarHostState)
                    }
                }

                is ProfileState.Error -> {
                    Text(
                        text = (state as ProfileState.Error).message
                            ?: stringResource(R.string.error),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = { Text(text = stringResource(R.string.confirm_change_password_title)) },
                    text = {
                        Text(
                            stringResource(R.string.confirm_change_password_text)
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.sendPasswordReset()
                            showConfirmDialog = false
                        }) {
                            Text(stringResource(R.string.confirm_yes_send))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            if (showTermsDialog) {
                AlertDialog(
                    onDismissRequest = {  },
                    title = { Text(stringResource(R.string.terms_title)) },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(stringResource(R.string.terms_text_intro))
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.terms_item1))
                            Spacer(Modifier.height(6.dp))
                            Text(stringResource(R.string.terms_item2))
                            Spacer(Modifier.height(6.dp))
                            Text(stringResource(R.string.terms_item3))
                            Spacer(Modifier.height(6.dp))
                            Text(stringResource(R.string.terms_item4))
                            Spacer(Modifier.height(6.dp))
                            Text(stringResource(R.string.terms_item5))
                            Spacer(Modifier.height(6.dp))
                            Text(stringResource(R.string.terms_item6))
                            Spacer(Modifier.height(6.dp))
                            Text(stringResource(R.string.terms_item7))
                            Spacer(Modifier.height(6.dp))
                            Text(stringResource(R.string.terms_item8))
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showTermsDialog = false }
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    },
                    dismissButton = {  }
                )
            }
        }
    }
}
