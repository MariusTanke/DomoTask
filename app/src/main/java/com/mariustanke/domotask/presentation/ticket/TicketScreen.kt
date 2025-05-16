package com.mariustanke.domotask.presentation.ticket

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mariustanke.domotask.domain.model.Comment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen(
    boardId: String,
    ticketId: String,
    viewModel: TicketViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadTicketAndComments(boardId, ticketId)
    }

    val ticket by viewModel.ticket.collectAsState()
    val comments by viewModel.comments.collectAsState()
    var newComment by remember { mutableStateOf("") }
    val currentUserId = viewModel.currentUser?.uid.orEmpty()

    ticket?.let {
        var title by remember { mutableStateOf(it.title) }
        var editingTitle by remember { mutableStateOf(false) }
        var description by remember { mutableStateOf(it.description) }
        var urgency by remember { mutableStateOf(it.urgency) }
        var assignedTo by remember { mutableStateOf(it.assignedTo) }
        var editingCommentId by remember { mutableStateOf<String?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (editingTitle) {
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { editingTitle = false }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Finalizar edición")
                                }
                            } else {
                                Text(title)
                                IconButton(onClick = { editingTitle = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar título")
                                }
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val userName by produceState(initialValue = "Cargando...", it.createdBy) {
                        viewModel.getUserName(it.createdBy) {
                            value = it
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = urgency,
                        onValueChange = { urgency = it },
                        label = { Text("Urgencia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = assignedTo,
                        onValueChange = { assignedTo = it },
                        label = { Text("Asignado a") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Creado por: $userName", style = MaterialTheme.typography.bodySmall)

                    Button(onClick = {
                        val updated = it.copy(
                            title = title,
                            description = description,
                            urgency = urgency,
                            assignedTo = assignedTo
                        )
                        viewModel.updateTicket(boardId, updated)
                    }) {
                        Text("Guardar cambios")
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Comentarios", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    comments.sortedByDescending { it.createdAt }.forEach { comment ->
                        val userName by produceState(initialValue = "Cargando...", comment.createdBy) {
                            viewModel.getUserName(comment.createdBy) {
                                value = it
                            }
                        }

                        CommentCard(
                            comment = comment,
                            currentUserId = currentUserId,
                            isEditing = editingCommentId == comment.id,
                            userName = userName,
                            onEditToggle = {
                                editingCommentId = if (editingCommentId == comment.id) null else comment.id
                            },
                            onEditConfirm = { newText ->
                                viewModel.updateComment(
                                    boardId,
                                    ticketId,
                                    comment.copy(
                                        content = newText,
                                        edited = true
                                    )
                                )
                                editingCommentId = null
                            },
                            onDeleteClick = {
                                viewModel.deleteComment(boardId, ticketId, comment.id)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        label = { Text("Nuevo comentario") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (newComment.isNotBlank()) {
                            viewModel.addComment(
                                boardId = boardId,
                                ticketId = ticketId,
                                comment = Comment(
                                    content = newComment,
                                    createdBy = currentUserId,
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                            newComment = ""
                        }
                    }) {
                        Text("Enviar")
                    }
                }
            }
        }
    }
}

@Composable
fun CommentCard(
    comment: Comment,
    currentUserId: String,
    userName: String,
    isEditing: Boolean,
    onEditToggle: () -> Unit,
    onEditConfirm: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    var editedText by remember(comment.id) { mutableStateOf(comment.content) }
    val isOwnComment = comment.createdBy == currentUserId
    val backgroundColor = if (isOwnComment)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isOwnComment) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    IconButton(onClick = onEditToggle) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Borrar")
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = editedText,
                        onValueChange = { editedText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Editar comentario") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { onEditConfirm(editedText) }) {
                            Text("Guardar")
                        }
                        TextButton(onClick = onEditToggle) {
                            Text("Cancelar")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isOwnComment) Arrangement.End else Arrangement.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = comment.content,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (comment.edited) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editado",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Nombre y fecha abajo a la derecha
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            if (!isOwnComment) {
                                Text(
                                    text = "Por: $userName",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Text(
                                text = "Fecha: ${formatDate(comment.createdAt)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    return java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(timestamp))
}
