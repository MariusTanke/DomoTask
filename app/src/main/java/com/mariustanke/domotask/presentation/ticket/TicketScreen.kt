package com.mariustanke.domotask.presentation.ticket

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mariustanke.domotask.domain.model.Comment
import com.mariustanke.domotask.domain.model.Ticket
import com.mariustanke.domotask.domain.model.User

@Composable
fun TicketScreen(
    boardId: String,
    ticketId: String,
    onBackClick: () -> Unit,
    viewModel: TicketViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadTicketAndComments(boardId, ticketId)
        viewModel.loadBoardMembers(boardId)
    }

    val ticket by viewModel.ticket.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val members by viewModel.members.collectAsState()
    val currentUserId = viewModel.currentUser?.uid.orEmpty()

    Log.d("DEBUG", members.toString())

    ticket?.let {
        TicketScaffold(
            ticket = it,
            comments = comments,
            members = members,
            currentUserId = currentUserId,
            onUpdateTicket = { updated -> viewModel.updateTicket(boardId, updated) },
            onAddComment = { comment -> viewModel.addComment(boardId, ticketId, comment) },
            onUpdateComment = { comment -> viewModel.updateComment(boardId, ticketId, comment) },
            onDeleteComment = { commentId -> viewModel.deleteComment(boardId, ticketId, commentId) },
            onBackClick = onBackClick,
            getUserName = viewModel::getUserName
        )
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun TicketScaffold(
    ticket: Ticket,
    comments: List<Comment>,
    members: List<User>,
    currentUserId: String,
    onUpdateTicket: (Ticket) -> Unit,
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
            TicketTopBar(
                title = title,
                editingTitle = editingTitle,
                onTitleClick = { editingTitle = true },
                onDoneClick = {
                    editingTitle = false
                    onUpdateTicket(
                        ticket.copy(
                            title = title,
                            description = description,
                            urgency = urgency,
                            assignedTo = assignedTo
                        )
                    )
                },
                onBackClick = onBackClick
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
                    title = title,
                    description = description,
                    urgency = urgency,
                    assignedTo = assignedTo,
                    members = members,
                    comments = comments,
                    currentUserId = currentUserId,
                    editingCommentId = editingCommentId,
                    onEditCommentToggle = { id -> editingCommentId = id },
                    onEditCommentConfirm = {
                        onUpdateComment(it)
                        editingCommentId = null
                    },
                    onDeleteComment = onDeleteComment,
                    onFieldChange = { newTitle, newDesc, newUrg, newAssigned ->
                        title = newTitle
                        description = newDesc
                        urgency = newUrg
                        assignedTo = newAssigned
                    },
                    getUserName = getUserName
                )
            }

            CommentInput(newComment = newComment, onCommentChange = { newComment = it }) {
                if (newComment.isNotBlank()) {
                    onAddComment(
                        Comment(
                            content = newComment,
                            createdBy = currentUserId,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    newComment = ""
                }
            }
        }
    }
}

@Composable
fun MemberDropdown(
    members: List<User>,
    selectedId: String,
    onMemberSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = members.find { it.id == selectedId }?.name ?: "Selecciona miembro"

    Box(modifier = Modifier
        .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Abrir menú",
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            members.forEach { member ->
                DropdownMenuItem(
                    text = { Text(member.name) },
                    onClick = {
                        onMemberSelected(member.id)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun TicketTopBar(
    title: String,
    editingTitle: Boolean,
    onTitleClick: () -> Unit,
    onDoneClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver atrás",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text(
            text = if (editingTitle) "Editando título" else title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.align(Alignment.Center).clickable { onTitleClick() }
        )
        IconButton(onClick = onDoneClick, modifier = Modifier.align(Alignment.CenterEnd)) {
            Icon(
                Icons.Default.Done,
                contentDescription = "Confirmar edición",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun TicketContent(
    ticket: Ticket,
    title: String,
    description: String,
    urgency: String,
    assignedTo: String,
    members: List<User>,
    comments: List<Comment>,
    currentUserId: String,
    editingCommentId: String?,
    onEditCommentToggle: (String?) -> Unit,
    onEditCommentConfirm: (Comment) -> Unit,
    onDeleteComment: (String) -> Unit,
    onFieldChange: (String, String, String, String) -> Unit,
    getUserName: (String, (String) -> Unit) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val creatorName by produceState(initialValue = "Cargando...", ticket.createdBy) {
            getUserName(ticket.createdBy) { value = it }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { onFieldChange(it, description, urgency, assignedTo) },
            label = { Text("Titulo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { onFieldChange(title, it, urgency, assignedTo) },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = urgency,
            onValueChange = { onFieldChange(title, description, it, assignedTo) },
            label = { Text("Urgencia") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        MemberDropdown(
            members = members,
            selectedId = assignedTo
        ) { newAssignedId ->
            onFieldChange(title, description, urgency, newAssignedId)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Creado por: $creatorName", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Comentarios", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        comments.sortedByDescending { it.createdAt }.forEach { comment ->
            val ticketUserName by produceState(initialValue = "Cargando...", comment.createdBy) {
                getUserName(comment.createdBy) { value = it }
            }

            CommentCard(
                comment = comment,
                currentUserId = currentUserId,
                userName = ticketUserName,
                isEditing = editingCommentId == comment.id,
                onEditToggle = {
                    onEditCommentToggle(if (editingCommentId == comment.id) null else comment.id)
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
            modifier = Modifier
                .size(48.dp)
                .padding(0.dp, 4.dp, 0.dp, 0.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
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
    val backgroundColor = if (isOwnComment) MaterialTheme.colorScheme.secondaryContainer
    else MaterialTheme.colorScheme.surfaceVariant

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
                    TextButton(onClick = { onEditConfirm(editedText) }) { Text("Guardar") }
                    TextButton(onClick = onEditToggle) { Text("Cancelar") }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = comment.content, style = MaterialTheme.typography.bodySmall)
                        if (comment.edited) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "(Editado)", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (!isOwnComment) {
                            Text(text = "Por: $userName", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (isOwnComment) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            IconButton(onClick = onEditToggle) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                            IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, contentDescription = "Borrar") }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
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