package com.mariustanke.domotask.presentation.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mariustanke.domotask.R

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val state by viewModel.registerState.collectAsState()

    var showTermsDialog by remember { mutableStateOf(true) }
    var termsAccepted by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is RegisterState.Success) {
            onRegisterSuccess()
        }
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { },
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
                    Spacer(Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Switch(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.terms_accept_label))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showTermsDialog = false },
                    enabled = termsAccepted
                ) {
                    Text(stringResource(R.string.terms_accept_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { onBackToLogin() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (!showTermsDialog) {
        Scaffold { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.register_title), style = MaterialTheme.typography.headlineMedium)

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.label_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.label_email)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.label_password)) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Check else Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.cd_toggle_password)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.register(name, email, password) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state is RegisterState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.button_register))
                        }
                    }

                    if (state is RegisterState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (state as RegisterState.Error).message ?: stringResource(R.string.error_unknown),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.login_prompt),
                        modifier = Modifier.clickable(onClick = onBackToLogin),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
