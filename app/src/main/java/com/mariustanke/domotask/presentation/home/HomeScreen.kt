package com.mariustanke.domotask.presentation.home

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mariustanke.domotask.domain.uiModels.BoardUiModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToBoard: (boardId: String) -> Unit
) {
    val user by viewModel.user.collectAsState()
    val boards by viewModel.boards.collectAsState()
    val invitations by viewModel.invitations.collectAsState()

    val context = LocalContext.current

    var showNewDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var newBoardName by remember { mutableStateOf("") }
    var newBoardDesc by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        viewModel.loadUser()
    }

    LaunchedEffect(viewModel.acceptRejectState.collectAsState().value) {
        viewModel.acceptRejectState.value?.let { result ->
            if (result.isSuccess) {
                Toast.makeText(context, "Acción completada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, result.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_SHORT).show()
            }
            viewModel.clearAcceptRejectState()
            viewModel.loadUser()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "INICIO",
                invitations = user?.invitations.orEmpty(),
                onInviteClick = {
                    showInviteDialog = true
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Crear tablero")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (boards.isEmpty()) {
                Text("No boards found", Modifier.align(Alignment.CenterHorizontally))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    boards.forEach { board ->
                        BoardCard(
                            board = board,
                            onClick = { onNavigateToBoard(board.id) }
                        )
                    }
                }
            }
        }

        if (showNewDialog) {
            AlertDialog(
                onDismissRequest = { showNewDialog = false },
                title = { Text("Nuevo tablero") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newBoardName,
                            onValueChange = { newBoardName = it },
                            label = { Text("Nombre del tablero") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newBoardDesc,
                            onValueChange = { newBoardDesc = it },
                            label = { Text("Descripción") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newBoardName.isNotBlank()) {
                            viewModel.createBoard(newBoardName.trim(), newBoardDesc)
                        }
                        showNewDialog = false
                        newBoardName = ""
                        newBoardDesc = ""
                    }) { Text("Crear") }
                },
                dismissButton = {
                    TextButton(onClick = { showNewDialog = false }) { Text("Cancelar") }
                }
            )
        }

        if (showInviteDialog) {
            AlertDialog(
                onDismissRequest = { showInviteDialog = false },
                title = { Text("Invitaciones pendientes") },
                text = {
                    Column(Modifier.animateContentSize()) {
                        invitations.forEach { boardUi ->
                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(boardUi.name, modifier = Modifier.weight(1f))

                                    IconButton(
                                        onClick = {
                                            viewModel.acceptInvitation(boardUi.id)
                                            showInviteDialog = false
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Aceptar invitación",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(Modifier.width(2.dp))

                                    IconButton(
                                        onClick = {
                                            viewModel.rejectInvitation(boardUi.id)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Rechazar invitación",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showInviteDialog = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}

@Composable
fun BoardCard(
    board: BoardUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(board.name, style = MaterialTheme.typography.titleLarge)
                if (board.description.isNotEmpty()) {
                    Text(
                        text = board.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Created by: ${board.createdByName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun TopBar(
    title: String,
    invitations: List<String> = emptyList(),
    onInviteClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
        )

        if (invitations.isNotEmpty()) {
            OutlinedButton(
                onClick = onInviteClick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                shape = MaterialTheme.shapes.small,
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = "Invitaciones",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(4.dp))
                Text("${invitations.size}", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}
