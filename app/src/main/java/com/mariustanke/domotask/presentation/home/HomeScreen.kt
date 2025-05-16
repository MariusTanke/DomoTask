package com.mariustanke.domotask.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mariustanke.domotask.domain.uiModels.BoardUiModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToBoard: (boardId: String, boardName: String) -> Unit
) {
    val boards by viewModel.boards.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var newBoardName by remember { mutableStateOf("") }
    var newBoardDescription by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Crear tablero")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (boards.isEmpty()) {
                Text(
                    text = "No boards found",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    boards.forEach { board ->
                        BoardCard(
                            board = board,
                            onClick = { onNavigateToBoard(board.id, board.name) },
                            onEdit = { /* mostrar diálogo de edición */ },
                            onDelete = { /* lanzar confirmación y luego borrar */ }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    newBoardName = ""
                },
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
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newBoardDescription,
                            onValueChange = { newBoardDescription = it },
                            label = { Text("Descripción del tablero") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            maxLines = 5
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newBoardName.isNotBlank()) {
                                viewModel.createBoard(newBoardName.trim(), newBoardDescription)
                            }
                            showDialog = false
                            newBoardName = ""
                            newBoardDescription = ""
                        }
                    ) {
                        Text("Crear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        newBoardName = ""
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun BoardCard(
    board: BoardUiModel,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

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

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        modifier = Modifier.size(32.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Borrar") },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}


