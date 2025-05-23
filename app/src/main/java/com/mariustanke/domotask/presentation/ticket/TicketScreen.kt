package com.mariustanke.domotask.presentation.ticket

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mariustanke.domotask.domain.enums.UrgencyEnum
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
    var urgency by remember { mutableIntStateOf(ticket.urgency) }
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
        },
        bottomBar = {
            CommentInput(
                newComment = newComment,
                onCommentChange = { newComment = it },
                onAddFileClick = {  },
                onSendClick = {
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
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                        urgency = newUrg.toInt()
                        assignedTo = newAssigned
                    },
                    getUserName = getUserName
                )
            }
        }
    }
}

@Composable
fun CommentInput(
    newComment: String,
    onCommentChange: (String) -> Unit,
    onAddFileClick: () -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newComment,
            onValueChange = onCommentChange,
            singleLine = false,
            label = { Text("Nuevo comentario") },
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 60.dp)
        )

        Spacer(Modifier.width(8.dp))

        FilledIconButton(
            onClick = onAddFileClick,
            modifier = Modifier
                .height(56.dp)
                .aspectRatio(1f)
                .padding(top = 6.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Icon(Icons.Default.AccountBox, contentDescription = "Adjuntar archivo")
        }

        Spacer(Modifier.width(8.dp))

        FilledIconButton(
            onClick = onSendClick,
            modifier = Modifier
                .height(56.dp)
                .aspectRatio(1f)
                .padding(top = 6.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
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
            modifier = Modifier
                .align(Alignment.Center)
                .clickable { onTitleClick() }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketContent(
    ticket: Ticket,
    title: String,
    description: String,
    urgency: Int,
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
    val isCreator = currentUserId == ticket.createdBy

    val urgencies = UrgencyEnum.entries
    var selectedUrgency by remember {
        mutableStateOf(
            urgencies.find { it.value == urgency } ?: UrgencyEnum.NORMAL
        )
    }
    var urgExpanded by remember { mutableStateOf(false) }

    var memExpanded by remember { mutableStateOf(false) }
    val assignedName = members.find { it.id == assignedTo }?.name
        ?: "Selecciona miembro"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val creatorName by produceState("Cargando...", ticket.createdBy) {
            getUserName(ticket.createdBy) { value = it }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = {
                onFieldChange(it, description, urgency.toString(), assignedTo)
            },
            label = { Text("Título") },
            enabled = isCreator,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = {
                onFieldChange(title, it, urgency.toString(), assignedTo)
            },
            label = { Text("Descripción") },
            enabled = isCreator,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5
        )
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = urgExpanded,
            onExpandedChange = {
                if (isCreator) urgExpanded = !urgExpanded
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedUrgency.label,
                onValueChange = {},
                readOnly = true,
                enabled = isCreator,
                label = { Text("Urgencia") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(urgExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .clickable(enabled = isCreator) { urgExpanded = true }
            )
            ExposedDropdownMenu(
                expanded = urgExpanded,
                onDismissRequest = { urgExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                urgencies.forEach { urg ->
                    DropdownMenuItem(
                        text = { Text(urg.label) },
                        enabled = isCreator,
                        onClick = {
                            selectedUrgency = urg
                            urgExpanded = false
                            onFieldChange(
                                title,
                                description,
                                urg.value.toString(),
                                assignedTo
                            )
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = memExpanded,
            onExpandedChange = {
                if (isCreator) memExpanded = !memExpanded
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = assignedName,
                onValueChange = {},
                readOnly = true,
                enabled = isCreator,
                label = { Text("Asignado a") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(memExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .clickable(enabled = isCreator) { memExpanded = true }
            )
            ExposedDropdownMenu(
                expanded = memExpanded,
                onDismissRequest = { memExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                members.forEach { member ->
                    DropdownMenuItem(
                        text = { Text(member.name) },
                        enabled = isCreator,
                        onClick = {
                            memExpanded = false
                            onFieldChange(
                                title,
                                description,
                                selectedUrgency.value.toString(),
                                member.id
                            )
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        Text("Creado por: $creatorName", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(32.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        Text("Comentarios", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        comments
            .sortedByDescending { it.createdAt }
            .forEach { comment ->
                val ticketUserName by produceState("Cargando...", comment.createdBy) {
                    getUserName(comment.createdBy) { value = it }
                }
                CommentCard(
                    comment = comment,
                    currentUserId = currentUserId,
                    userName = ticketUserName,
                    isEditing = editingCommentId == comment.id,
                    onEditToggle = {
                        onEditCommentToggle(
                            if (editingCommentId == comment.id) null else comment.id
                        )
                    },
                    onEditConfirm = { newText ->
                        onEditCommentConfirm(
                            comment.copy(content = newText, edited = true)
                        )
                    },
                    onDeleteClick = { onDeleteComment(comment.id) }
                )
            }
    }
}

@OptIn(ExperimentalFoundationApi::class)
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

    var showMenu by remember { mutableStateOf(false) }

    val bubbleColor = if (isOwnComment)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val bubbleShape = if (isOwnComment) {
        RoundedCornerShape(
            topStart = 12.dp, topEnd = 12.dp,
            bottomStart = 12.dp, bottomEnd = 0.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 12.dp, topEnd = 12.dp,
            bottomStart = 0.dp,   bottomEnd = 12.dp
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isOwnComment) Arrangement.End else Arrangement.Start
    ) {
        Box {
            Surface(
                color = bubbleColor,
                shape = bubbleShape,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .padding(horizontal = 8.dp)
                    .combinedClickable(
                        enabled = isOwnComment && !isEditing,
                        onClick = {},
                        onLongClick = { showMenu = true }
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editedText,
                            onValueChange = { editedText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Editar comentario") }
                        )
                        Spacer(Modifier.height(8.dp))
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
                        Text(
                            text = comment.content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )

                        if (comment.edited) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "(Editado)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        if (!isOwnComment) {
                            Text(
                                text = "Por: $userName",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(2.dp))
                        }

                        Text(
                            text = "Fecha: ${formatDate(comment.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                }
            }

            if (showMenu) {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            showMenu = false
                            onEditToggle()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Borrar") },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}


fun formatDate(timestamp: Long): String {
    return java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(timestamp))
}