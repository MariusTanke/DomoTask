package com.mariustanke.domotask.presentation.home

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mariustanke.domotask.domain.uiModels.BoardUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToBoard: (boardId: String, boardName: String) -> Unit
) {
    val user by viewModel.user.collectAsState()
    val boards by viewModel.boards.collectAsState()

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
            TopAppBar(
                title = { Text("INICIO") },
                actions = {
                    if (user?.invitations?.isNotEmpty() == true) {
                        IconButton(onClick = { showInviteDialog = true }) {
                            Icon(Icons.Default.Email, contentDescription = "Invitaciones")
                        }
                    }
                }
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
                            onClick = { onNavigateToBoard(board.id, board.name) }
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
                        user?.invitations?.forEach { board ->
                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(board, modifier = Modifier.weight(1f))
                                    TextButton(onClick = {
                                        viewModel.acceptInvitation(board)
                                        showInviteDialog = false

                                    }) { Text("Aceptar") }
                                    TextButton(onClick = {
                                        viewModel.rejectInvitation(board)
                                    }) { Text("Rechazar") }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showInviteDialog = false }) { Text("Cerrar") }
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


