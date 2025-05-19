package com.mariustanke.domotask.presentation.ticket

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mariustanke.domotask.domain.model.Comment

@Composable
fun TicketScreen(
    boardId: String,
    ticketId: String,
    onBackClick: () -> Unit,
    viewModel: TicketViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadTicketAndComments(boardId, ticketId)
    }

    val ticket by viewModel.ticket.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val currentUserId = viewModel.currentUser?.uid.orEmpty()

    ticket?.let {
        TicketScaffold(
            ticket = it,
            comments = comments,
            currentUserId = currentUserId,
            onUpdateTicket = { updated -> viewModel.updateTicket(boardId, updated) },
            onAddComment = { comment -> viewModel.addComment(boardId, ticketId, comment) },
            onUpdateComment = { comment -> viewModel.updateComment(boardId, ticketId, comment) },
            onDeleteComment = { commentId -> viewModel.deleteComment(boardId, ticketId, commentId) },
            onBackClick = { onBackClick() },
            getUserName = viewModel::getUserName
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScaffold(
    ticket: com.mariustanke.domotask.domain.model.Ticket,
    comments: List<Comment>,
    currentUserId: String,
    onUpdateTicket: (com.mariustanke.domotask.domain.model.Ticket) -> Unit,
    onAddComment: (Comment) -> Unit,
    onUpdateComment: (Comment) -> Unit,
    onDeleteComment: (String) -> Unit,
    onBackClick: () -> Unit,
    getUserName: (String, (String) -> Unit) -> Unit
) {
    var title by remember { mutableStateOf(ticket.title) }
    var editingTitle by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf(ticket.description) }
    var urgency by remember { mutableStateOf(ticket.urgency) }
    var assignedTo by remember { mutableStateOf(ticket.assignedTo) }
    var newComment by remember { mutableStateOf("") }
    var editingCommentId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver atrás")
                    }
                },
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (editingTitle) {
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    textStyle = MaterialTheme.typography.titleLarge,
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        containerColor = Color.Transparent,
                                        focusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        cursorColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                                IconButton(
                                    onClick = { editingTitle = false },
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Finalizar edición")
                                }
                            } else {
                                Text(
                                    text = title,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                IconButton(
                                    onClick = { editingTitle = true },
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar título")
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                TicketContent(
                    ticket = ticket,
                    description = description,
                    urgency = urgency,
                    assignedTo = assignedTo,
                    comments = comments,
                    currentUserId = currentUserId,
                    editingCommentId = editingCommentId,
                    onEditCommentToggle = { id -> editingCommentId = id },
                    onEditCommentConfirm = {
                        onUpdateComment(it)
                        editingCommentId = null
                    },
                    onDeleteComment = onDeleteComment,
                    onUpdateTicket = {
                        onUpdateTicket(ticket.copy(
                            title = title,
                            description = description,
                            urgency = urgency,
                            assignedTo = assignedTo
                        ))
                    },
                    onFieldChange = { newDesc, newUrgency, newAssigned ->
                        description = newDesc
                        urgency = newUrgency
                        assignedTo = newAssigned
                    },
                    getUserName = getUserName
                )
            }

            CommentInput(newComment = newComment, onCommentChange = { newComment = it }) {
                if (newComment.isNotBlank()) {
                    onAddComment(Comment(
                        content = newComment,
                        createdBy = currentUserId,
                        createdAt = System.currentTimeMillis()
                    ))
                    newComment = ""
                }
            }
        }
    }
}

@Composable
fun TicketContent(
    ticket: com.mariustanke.domotask.domain.model.Ticket,
    description: String,
    urgency: String,
    assignedTo: String,
    comments: List<Comment>,
    currentUserId: String,
    editingCommentId: String?,
    onEditCommentToggle: (String?) -> Unit,
    onEditCommentConfirm: (Comment) -> Unit,
    onDeleteComment: (String) -> Unit,
    onUpdateTicket: () -> Unit,
    onFieldChange: (String, String, String) -> Unit,
    getUserName: (String, (String) -> Unit) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val userName by produceState(initialValue = "Cargando...", ticket.createdBy) {
            getUserName(ticket.createdBy) {
                value = it
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { onFieldChange(it, urgency, assignedTo) },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = urgency,
            onValueChange = { onFieldChange(description, it, assignedTo) },
            label = { Text("Urgencia") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = assignedTo,
            onValueChange = { onFieldChange(description, urgency, it) },
            label = { Text("Asignado a") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Creado por: $userName", style = MaterialTheme.typography.bodySmall)

        Button(onClick = onUpdateTicket) {
            Text("Guardar cambios")
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Comentarios", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        comments.sortedByDescending { it.createdAt }.forEach { comment ->
            val ticketUserName by produceState(initialValue = "Cargando...", comment.createdBy) {
                getUserName(comment.createdBy) {
                    value = it
                }
            }

            CommentCard(
                comment = comment,
                currentUserId = currentUserId,
                isEditing = editingCommentId == comment.id,
                userName = ticketUserName,
                onEditToggle = {
                    onEditCommentToggle(
                        if (editingCommentId == comment.id) null else comment.id
                    )
                },
                onEditConfirm = { newText ->
                    onEditCommentConfirm(comment.copy(content = newText, edited = true))
                },
                onDeleteClick = {
                    onDeleteComment(comment.id)
                }
            )
        }
    }
}

@Composable
fun CommentInput(
    newComment: String,
    onCommentChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newComment,
            onValueChange = onCommentChange,
            label = { Text("Nuevo comentario") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        FilledIconButton(
            onClick = onSendClick,
            modifier = Modifier.size(48.dp).padding(0.dp, 4.dp, 0.dp, 0.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Enviar"
            )
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = comment.content,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (comment.edited) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "(Editado)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (!isOwnComment) {
                            Text(
                                text = "Por: $userName",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (isOwnComment) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            IconButton(onClick = onEditToggle) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = onDeleteClick) {
                                Icon(Icons.Default.Delete, contentDescription = "Borrar")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp)) // espacio antes de la fecha

                Text(
                    text = "Fecha: ${formatDate(comment.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    return java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(timestamp))
}
