package com.mariustanke.domotask.presentation.ticket

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.mariustanke.domotask.R
import com.mariustanke.domotask.domain.enums.UrgencyEnum
import com.mariustanke.domotask.domain.model.Comment
import com.mariustanke.domotask.domain.model.Ticket
import com.mariustanke.domotask.domain.model.User
import com.mariustanke.domotask.presentation.board.initials
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TicketScreen(
    boardId: String,
    ticketId: String,
    onBackToBoardClick: () -> Unit,
    viewModel: TicketViewModel = hiltViewModel(),
    onSubTicketClick: (boardId: String, ticketId: String) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadTicketAndComments(boardId, ticketId)
        viewModel.loadBoardMembers(boardId)
    }

    val ticket by viewModel.ticket.collectAsState()
    val subTickets by viewModel.subTickets.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val members by viewModel.members.collectAsState()
    val currentUserId = viewModel.currentUser?.uid.orEmpty()
    var showDialog by remember { mutableStateOf(false) }
    val onCreateRelatedClick = { showDialog = true }
    val statuses by viewModel.statuses.collectAsState()

    ticket?.let {
        TicketScaffold(
            ticket = it,
            boardId = boardId,
            comments = comments,
            subTickets = subTickets,
            members = members,
            currentUserId = currentUserId,
            onUpdateTicket = { updated -> viewModel.updateTicket(boardId, updated) },
            onAddComment = { comment ->
                viewModel.sendComment(boardId, ticketId, comment.content, null)
            },
            onUpdateComment = { comment -> viewModel.updateComment(boardId, ticketId, comment) },
            onDeleteComment = { comment ->
                viewModel.deleteComment(
                    boardId,
                    ticketId,
                    comment
                )
            },
            onBackClick = onBackToBoardClick,
            getUserName = viewModel::getUserName,
            onAddCommentWithImage = { uri, text ->
                viewModel.sendComment(boardId, ticketId, text, uri)
            },
            showTicketDialog = onCreateRelatedClick,
            onSubTicketClick = onSubTicketClick,
        )
    }

    if (showDialog) {
        CreateTicketDialog(
            onCreate = { title, desc, urg, assigned ->
                viewModel.createTicket(
                    boardId,
                    Ticket(
                        title = title,
                        description = desc,
                        urgency = urg,
                        createdBy = viewModel.currentUser?.uid.orEmpty(),
                        assignedTo = assigned,
                        parentId = ticketId,
                        createdAt = System.currentTimeMillis(),
                        status = statuses.firstOrNull()?.id.orEmpty()
                    )
                )
                showDialog = false
            },
            members = members,
            onDismiss = { showDialog = false }
        )
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun TicketScaffold(
    ticket: Ticket,
    boardId: String,
    subTickets: List<Ticket?>,
    comments: List<Comment>,
    members: List<User>,
    currentUserId: String,
    onUpdateTicket: (Ticket) -> Unit,
    onAddComment: (Comment) -> Unit,
    onAddCommentWithImage: (Uri, String) -> Unit,
    onUpdateComment: (Comment) -> Unit,
    onDeleteComment: (Comment) -> Unit,
    onBackClick: () -> Unit,
    getUserName: (String, (String) -> Unit) -> Unit,
    showTicketDialog: () -> Unit,
    onSubTicketClick: (boardId: String, ticketId: String) -> Unit
) {
    var title by remember { mutableStateOf(ticket.title) }
    var editingTitle by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf(ticket.description) }
    var urgency by remember { mutableIntStateOf(ticket.urgency) }
    var assignedTo by remember { mutableStateOf(ticket.assignedTo) }
    var newComment by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var editingCommentId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TicketTopBar(
                title = title,
                editingTitle = editingTitle,
                isOwnComment = ticket.createdBy == currentUserId,
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
                    onBackClick()
                },
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            CommentInput(
                newComment = newComment,
                onCommentChange = { newComment = it },
                selectedImageUri = selectedImageUri,
                onImageSelected = { uri -> selectedImageUri = uri },
                onSendClick = {
                    if (newComment.isNotBlank()) {
                        selectedImageUri?.let { uri ->
                            onAddCommentWithImage(uri, newComment)
                        } ?: onAddComment(
                            Comment(
                                content = newComment,
                                createdBy = currentUserId,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                        newComment = ""
                        selectedImageUri = null
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
                    boardId = boardId,
                    subTickets = subTickets,
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
                    onBackClick = onBackClick,
                    getUserName = getUserName,
                    showTicketDialog = showTicketDialog,
                    onSubTicketClick = onSubTicketClick
                )
            }
        }
    }
}

@Composable
fun CommentInput(
    newComment: String,
    onCommentChange: (String) -> Unit,
    selectedImageUri: Uri?,
    onImageSelected: (Uri) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let(onImageSelected) }

    val photoFile = remember {
        File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
    }

    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean -> if (success) onImageSelected(photoUri) }
    var showPickerMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        selectedImageUri?.let { uri ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(uri)
                    .crossfade(true)
                    .transformations(RoundedCornersTransformation(8f))
                    .build(),
                contentDescription = "Imagen adjunta",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newComment,
                onValueChange = onCommentChange,
                singleLine = false,
                label = { Text(stringResource(R.string.comment_label_new)) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 60.dp)
            )

            Spacer(Modifier.width(8.dp))

            Box {
                FilledIconButton(
                    onClick = { showPickerMenu = true },
                    modifier = Modifier
                        .height(56.dp)
                        .aspectRatio(1f)
                        .padding(top = 6.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_upload_file),
                        contentDescription = "Adjuntar imagen"
                    )
                }
                DropdownMenu(
                    expanded = showPickerMenu,
                    onDismissRequest = { showPickerMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.comment_option_gallery)) },
                        onClick = {
                            showPickerMenu = false
                            galleryLauncher.launch("image/*")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.comment_option_camera)) },
                        onClick = {
                            showPickerMenu = false
                            cameraLauncher.launch(photoUri)
                        }
                    )
                }
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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.send)
                )
            }
        }
    }
}

@Composable
fun TicketTopBar(
    title: String,
    isOwnComment: Boolean,
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
                contentDescription = stringResource(R.string.topbar_back),
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
        if (isOwnComment) {
            OutlinedButton(
                onClick = onDoneClick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_save),
                    contentDescription = stringResource(R.string.topbar_confirm_edit),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketContent(
    ticket: Ticket,
    boardId: String,
    subTickets: List<Ticket?>,
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
    onDeleteComment: (Comment) -> Unit,
    onFieldChange: (String, String, String, String) -> Unit,
    getUserName: (String, (String) -> Unit) -> Unit,
    showTicketDialog: () -> Unit,
    onBackClick: () -> Unit,
    onSubTicketClick: (boardId: String, ticketId: String) -> Unit
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
        ?: stringResource(R.string.create_ticket_placeholder_select_member)

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
            label = { Text(stringResource(R.string.title)) },
            enabled = isCreator,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = {
                onFieldChange(title, it, urgency.toString(), assignedTo)
            },
            label = { Text(stringResource(R.string.description)) },
            enabled = isCreator,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            minLines = 6,
            maxLines = 12,
            singleLine = false
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
                label = { Text(stringResource(R.string.urgency)) },
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
                label = { Text(stringResource(R.string.create_ticket_label_assigned_to)) },
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
                        enabled = isCreator,
                        leadingIcon = {
                            if (member.photo != null) {
                                AsyncImage(
                                    model = member.photo,
                                    contentDescription = stringResource(R.string.cd_member_photo, member.name),
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    placeholder = painterResource(R.drawable.placeholder_avatar),
                                    error = painterResource(R.drawable.placeholder_avatar),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.name.split(" ")
                                            .filter { it.isNotBlank() }
                                            .map { it.first().uppercaseChar() }
                                            .take(2)
                                            .joinToString(""),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        },
                        text = { Text(member.name) },
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
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        if (ticket.parentId.isNullOrEmpty()) {
            Button(
                onClick = { showTicketDialog() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_create_related))
            }

            if (subTickets.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.label_subtasks), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Column {
                    subTickets.forEach { sub ->
                        if (sub != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onSubTicketClick(boardId, sub.id)
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .background(
                                                color = when (sub.urgency) {
                                                    in 1..2 -> Color.Green
                                                    3 -> Color.Yellow
                                                    else -> Color.Red
                                                },
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(sub.title, style = MaterialTheme.typography.bodyLarge)
                                        Text(
                                            "Asignado a: ${
                                                members.find { it.id == sub.assignedTo }?.name ?: "—"
                                            }",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } else {
            Button(
                onClick = { onSubTicketClick(boardId, ticket.parentId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_go_to_parent))
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
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
                    onDeleteClick = { onDeleteComment(comment) }
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
    var showMenu by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf(false) }

    val bubbleColor = if (isOwnComment)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val bubbleShape = if (isOwnComment) {
        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 0.dp)
    } else {
        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 12.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isOwnComment) Arrangement.End else Arrangement.Start
    ) {
        Box {
            if (showFullImage && comment.imageUrl != null) {
                Dialog(onDismissRequest = { showFullImage = false }) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(comment.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.dialog_image_enlarged),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp)
                                .clickable { showFullImage = false }
                        )

                    }
                }
            }

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
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.Start) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editedText,
                            onValueChange = { editedText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.comment_edit_label)) }
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = {
                                onEditConfirm(editedText)
                            }) { Text(stringResource(R.string.save)) }
                            TextButton(onClick = onEditToggle) { Text(stringResource(R.string.cancel)) }
                        }
                    } else {
                        comment.imageUrl?.let { imageUrl ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .placeholder(R.drawable.placeholder_image)
                                    .transformations(RoundedCornersTransformation(8f))
                                    .build(),
                                contentDescription = stringResource(R.string.dialog_image_comment),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp, max = 200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showFullImage = true },
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        Text(
                            text = comment.content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 16.sp
                            )
                        )

                        if (comment.edited) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.label_edited),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        if (!isOwnComment) {
                            Text(
                                text = stringResource(R.string.label_comment_by, userName),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(2.dp))
                        }

                        Text(
                            text = stringResource(R.string.label_comment_date, formatDate(comment.createdAt)),
                            style = MaterialTheme.typography.bodySmall
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
                        text = { Text(stringResource(R.string.edit)) },
                        onClick = {
                            showMenu = false
                            onEditToggle()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketDialog(
    onCreate: (title: String, desc: String, urgencyLevel: Int, assignedTo: String) -> Unit,
    members: List<User>,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val urgencies = UrgencyEnum.entries
    var selectedUrgency by remember { mutableStateOf(UrgencyEnum.NORMAL) }

    var urgExpanded by remember { mutableStateOf(false) }
    var memExpanded by remember { mutableStateOf(false) }
    var assignedToId by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_ticket_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    maxLines = 5
                )

                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = urgExpanded,
                    onExpandedChange = { urgExpanded = !urgExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedUrgency.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.urgency)) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .clickable { urgExpanded = true }
                    )
                    ExposedDropdownMenu(
                        expanded = urgExpanded,
                        onDismissRequest = { urgExpanded = false }
                    ) {
                        urgencies.forEach { urg ->
                            DropdownMenuItem(
                                text = { Text(urg.label) },
                                onClick = {
                                    selectedUrgency = urg
                                    urgExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = memExpanded,
                    onExpandedChange = {
                        memExpanded = !memExpanded
                        if (memExpanded) {
                            focusRequester.requestFocus()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = members.find { it.id == assignedToId }?.name
                            ?: stringResource(R.string.create_ticket_placeholder_select_member),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.create_ticket_label_assigned_to)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .clickable { memExpanded = true }
                    )
                    ExposedDropdownMenu(
                        expanded = memExpanded,
                        onDismissRequest = { memExpanded = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    if (member.photo != null) {
                                        AsyncImage(
                                            model = member.photo,
                                            contentDescription = stringResource(R.string.cd_member_photo, member.name),
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape),
                                            placeholder = painterResource(R.drawable.placeholder_avatar),
                                            error = painterResource(R.drawable.placeholder_avatar),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = initials(member.name),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                },
                                text = { Text(member.name) },
                                onClick = {
                                    assignedToId = member.id
                                    memExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onCreate(
                    title,
                    description,
                    selectedUrgency.value,
                    assignedToId
                )
            }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        .format(Date(timestamp))
}