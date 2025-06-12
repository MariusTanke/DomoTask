package com.mariustanke.domotask.presentation.board

import android.annotation.SuppressLint
import android.widget.Space
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mariustanke.domotask.R
import com.mariustanke.domotask.domain.enums.UrgencyEnum
import com.mariustanke.domotask.domain.model.Status
import com.mariustanke.domotask.domain.model.Ticket
import com.mariustanke.domotask.domain.model.User

@Composable
fun BoardScreen(
    boardId: String,
    viewModel: BoardViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onTicketClick: (boardId: String, ticketId: String) -> Unit
) {
    val context = LocalContext.current

    var selectedFilter by remember { mutableStateOf<String?>(null) }

    var showAddStatusDialog by remember { mutableStateOf(false) }
    var newStatusName by remember { mutableStateOf("") }
    var newStatusOrder by remember { mutableIntStateOf(1) }
    var showDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showMemberDialog by remember { mutableStateOf(false) }
    var inviteInput by remember { mutableStateOf("") }

    var memberToDelete by remember { mutableStateOf<User?>(null) }
    var removeUserTickets by remember { mutableStateOf(false) }

    val boardData by viewModel.board.collectAsState()
    val tickets by viewModel.tickets.collectAsState()
    val statuses by viewModel.statuses.collectAsState()
    val members by viewModel.members.collectAsState()
    val inviteState by viewModel.inviteState.collectAsState()

    val currentUserId = viewModel.currentUser?.uid

    val previousMembers = remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(members) {
        val prev = previousMembers.value
        val curr = members

        if (currentUserId != null) {
            val wasInPrev = prev.any { it.id == currentUserId }
            val isInCurr = curr.any { it.id == currentUserId }
            if (wasInPrev && !isInCurr) {
                onBackClick()
            }
        }

        previousMembers.value = curr
    }

    LaunchedEffect(inviteState) {
        inviteState?.let { result ->
            if (result.isSuccess) {
                Toast.makeText(context, "Invitación enviada", Toast.LENGTH_SHORT).show()
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Error al invitar"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
            viewModel.clearInviteState()
        }
    }

    LaunchedEffect(boardId) {
        viewModel.loadBoard(boardId)
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            currentUserId?.let { id ->
                TopBar(
                    boardName = boardData?.name ?: "",
                    showMenu = showMenu,
                    onBackClick = onBackClick,
                    onMemberManagementClick = {
                        showMemberDialog = true
                        showMenu = false
                    },
                    onMenuClick = { showMenu = true },
                    onDismissMenu = { showMenu = false },
                    boardOwnerId = boardData?.createdBy ?: "",
                    onLeaveBoard = {
                        viewModel.removeMember(
                            boardId,
                            id,
                            removeUserTickets
                        )
                    },
                    onDeleteBoard = {
                        viewModel.deleteBoard(boardId)
                        onBackClick()
                    },
                    currentUserId = id,
                    onAddStatusClick = {
                        showAddStatusDialog = true
                        showMenu = false
                    },
                    onFilterSelected = { selectedFilter = it },
                    members = members
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_ticket))
            }
        }
    ) { innerPadding ->

        val ticketsFiltered = tickets.filter { ticket ->
            when (selectedFilter) {
                null -> true
                else -> ticket.assignedTo == selectedFilter
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 8.dp)
        ) {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                statuses.sortedBy { it.order }.forEach { status ->
                    val columnTickets = ticketsFiltered.filter { it.status == status.id }
                    boardData?.let { board ->
                        TicketColumn(
                            status = status,
                            members = members,
                            tickets = columnTickets,
                            allStatuses = statuses,
                            viewModel = viewModel,
                            boardId = boardId,
                            onTicketClick = onTicketClick,
                            boardOwnerId = board.createdBy
                        )
                    }
                }
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
                                createdBy = currentUserId.orEmpty(),
                                assignedTo = assigned,
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

            if (showAddStatusDialog) {
                AlertDialog(
                    onDismissRequest = { showAddStatusDialog = false },
                    title = { Text(stringResource(R.string.add_status)) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newStatusName,
                                onValueChange = { newStatusName = it },
                                label = { Text(stringResource(R.string.add_status_label_name)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newStatusOrder.toString(),
                                onValueChange = {
                                    newStatusOrder = it.toIntOrNull() ?: newStatusOrder
                                },
                                label = { Text(stringResource(R.string.order)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.createBoardStatus(
                                boardId,
                                Status(name = newStatusName, order = newStatusOrder)
                            )
                            showAddStatusDialog = false
                        }) { Text(stringResource(R.string.add)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddStatusDialog = false }) { Text(stringResource(R.string.cancel)) }
                    }
                )
            }

            if (showMemberDialog) {
                val maxVisible = 5
                val listModifier = if (members.size > maxVisible) {
                    Modifier
                        .height(200.dp)
                        .verticalScroll(rememberScrollState())
                } else Modifier.wrapContentHeight()

                AlertDialog(
                    onDismissRequest = { showMemberDialog = false },
                    title = { Text(stringResource(R.string.members_title)) },
                    text = {
                        Column(
                            modifier = Modifier
                                .animateContentSize()
                                .padding(8.dp)
                        ) {
                            Column(modifier = listModifier) {
                                members.forEach { user ->
                                    Card(
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        elevation = CardDefaults.cardElevation(2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp)
                                        ) {
                                            if (user.photo != null) {
                                                AsyncImage(
                                                    model = user.photo,
                                                    contentDescription = stringResource(R.string.cd_avatar_of, user.name),
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape),
                                                    placeholder = painterResource(R.drawable.placeholder_avatar),
                                                    error = painterResource(R.drawable.placeholder_avatar),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = stringResource(id = R.string.cd_avatar),
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = user.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.weight(1f)
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .padding(start = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (user.id != currentUserId) {
                                                    IconButton(onClick = {
                                                        memberToDelete = user
                                                        removeUserTickets = false
                                                    }) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = stringResource(R.string.delete_member_option),
                                                            modifier = Modifier.size(28.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = inviteInput,
                                onValueChange = { inviteInput = it },
                                label = { Text(stringResource(R.string.invite_member_label)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.inviteMember(boardId, inviteInput)
                            inviteInput = ""
                        }) {
                            Text(stringResource(R.string.invite_member_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showMemberDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            memberToDelete?.let { user ->
                AlertDialog(
                    onDismissRequest = {
                        memberToDelete = null
                        removeUserTickets = false
                    },
                    title = { Text(stringResource(R.string.confirm_delete_title)) },
                    text = {
                        Column {
                            Text(stringResource(R.string.confirm_delete_member_text, user.name))
                            Spacer(Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.delete_member_option),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = removeUserTickets,
                                    onCheckedChange = { removeUserTickets = it }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.removeMember(boardId, user.id, removeUserTickets)
                            memberToDelete = null
                            removeUserTickets = false
                        }) {
                            Text(stringResource(R.string.yes_delete))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            memberToDelete = null
                            removeUserTickets = false
                        }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}


@SuppressLint("UnrememberedMutableState")
@Composable
fun TopBar(
    boardName: String,
    showMenu: Boolean,
    members: List<User>,
    onMemberManagementClick: () -> Unit,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onAddStatusClick: () -> Unit,
    onBackClick: () -> Unit,
    currentUserId: String,
    boardOwnerId: String,
    onLeaveBoard: () -> Unit,
    onDeleteBoard: () -> Unit,
    onFilterSelected: (String?) -> Unit,
) {
    var barWidthPx by remember { mutableIntStateOf(0) }
    var menuWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val offsetDp by derivedStateOf {
        val dx = (barWidthPx - menuWidthPx).coerceAtLeast(0)
        with(density) { dx.toDp() }
    }
    var showFilterMenu by remember { mutableStateOf(false) }

    var showConfirmDeleteBoardDialog by remember { mutableStateOf(false) }
    var showConfirmLeaveDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .onGloballyPositioned { barWidthPx = it.size.width }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = boardName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f) // Ocupa el espacio restante
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showFilterMenu = true },
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = stringResource(R.string.filter),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                OutlinedButton(
                    onClick = onMenuClick,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showFilterMenu,
            onDismissRequest = { showFilterMenu = false },
            offset = DpOffset(x = (-40).dp, y = 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_all)) },
                onClick = {
                    onFilterSelected(null)
                    showFilterMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_unassigned)) },
                onClick = {
                    onFilterSelected("")
                    showFilterMenu = false
                }
            )
            HorizontalDivider()
            members.forEach { user ->
                DropdownMenuItem(
                    text = { Text(user.name) },
                    onClick = {
                        onFilterSelected(user.id)
                        showFilterMenu = false
                    }
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onDismissMenu,
            offset = DpOffset(x = offsetDp, y = 0.dp),
            modifier = Modifier.onGloballyPositioned { menuWidthPx = it.size.width }
        ) {
            if (currentUserId == boardOwnerId) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_manage_members)) },
                    onClick = onMemberManagementClick
                )
                HorizontalDivider()

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_edit_board)) },
                    onClick = onDismissMenu
                )
                HorizontalDivider()

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.add_status)) },
                    onClick = onAddStatusClick
                )
                HorizontalDivider()

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_delete_board)) },
                    onClick = {
                        showConfirmDeleteBoardDialog = true
                        onDismissMenu()
                    }
                )
            } else {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_leave_board)) },
                    onClick = {
                        showConfirmLeaveDialog = true
                        onDismissMenu()
                    }
                )
            }
        }
    }

    if (showConfirmDeleteBoardDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteBoardDialog = false },
            title = { Text(text = stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_board_text)) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteBoard()
                    showConfirmDeleteBoardDialog = false
                }) {
                    Text(stringResource(R.string.yes_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteBoardDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showConfirmLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmLeaveDialog = false },
            title = { Text(text = stringResource(R.string.confirm_leave_title)) },
            text = { Text(stringResource(R.string.confirm_leave_text)) },
            confirmButton = {
                TextButton(onClick = {
                    onLeaveBoard()
                    onBackClick()
                    showConfirmLeaveDialog = false
                }) {
                    Text(stringResource(R.string.confirm_leave_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmLeaveDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun TicketColumn(
    status: Status,
    tickets: List<Ticket>,
    members: List<User>,
    allStatuses: List<Status>,
    viewModel: BoardViewModel,
    boardId: String,
    boardOwnerId: String,
    onTicketClick: (boardId: String, ticketId: String) -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(status.name) }
    var editOrder by remember { mutableIntStateOf(status.order) }

    var showDeleteStatusDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            if (viewModel.currentUser?.uid == boardOwnerId) {
                if (editing) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text(stringResource(R.string.name)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = editOrder.toString(),
                            onValueChange = { editOrder = it.toIntOrNull() ?: editOrder },
                            label = { Text(stringResource(R.string.order)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = {
                                viewModel.updateBoardStatus(
                                    boardId,
                                    status.copy(name = editName, order = editOrder)
                                )
                                editing = false
                            }) {
                                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save))
                            }
                            IconButton(onClick = {
                                editName = status.name
                                editOrder = status.order
                                editing = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                            }
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(status.name, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { editing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.ticketcol_edit_column))
                        }
                        if (tickets.isEmpty()) {
                            IconButton(onClick = { showDeleteStatusDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.ticketcol_delete_column))
                            }
                        }
                    }
                }
            } else {
                Text(status.name, style = MaterialTheme.typography.titleLarge)
            }

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )

            tickets.forEach { ticket ->
                val assignedUser = members.find { it.id == ticket.assignedTo }
                PostItTicketCard(
                    ticket = ticket,
                    allStatuses = allStatuses,
                    currentUserId = viewModel.currentUser?.uid.orEmpty(),
                    assignedToName = assignedUser?.name ?: "",
                    onClick = { onTicketClick(boardId, ticket.id) },
                    onMove = { newStatus ->
                        viewModel.updateTicket(boardId, ticket.copy(status = newStatus))
                    },
                    assignedToPhoto = assignedUser?.photo ?: "",
                    onDelete = { viewModel.deleteTicket(boardId, ticket.id) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showDeleteStatusDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteStatusDialog = false },
            title = { Text(text = stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_column_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeBoardStatus(boardId, status.id)
                    showDeleteStatusDialog = false
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteStatusDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }
}

fun initials(name: String): String {
    return name
        .split(" ")
        .filter { it.isNotBlank() }
        .map { it.first().uppercaseChar() }
        .take(2)
        .joinToString("")
}

@Composable
fun PostItTicketCard(
    ticket: Ticket,
    assignedToName: String,
    assignedToPhoto: String?,
    allStatuses: List<Status>,
    currentUserId: String,
    onClick: () -> Unit,
    onMove: (newStatus: String) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val urgencyColor = when (ticket.urgency) {
        1 -> Color(0xFFE8F5E9)
        2 -> Color(0xFFFFFDE7)
        3 -> Color(0xFFFFF9C4)
        4 -> Color(0xFFFFE0B2)
        5 -> Color(0xFFFFCDD2)
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        if (ticket.createdBy == currentUserId) {
                            expanded = true
                        }
                    }
                ),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = urgencyColor),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.05f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 16.dp
                        )
                ) {
                    Text(
                        text = ticket.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    if (ticket.description.isNotBlank()) {
                        Text(
                            text = ticket.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (assignedToName.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(Modifier.weight(1f))
                            if (!assignedToPhoto.isNullOrBlank()) {
                                AsyncImage(
                                    model = assignedToPhoto,
                                    contentDescription = stringResource(id = R.string.cd_avatar_of, assignedToName),
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
                                        text = initials(assignedToName),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            allStatuses
                .filter { it.id != ticket.status }
                .sortedBy { it.order }
                .forEach { statusItem ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.move_to, statusItem.name)) },
                        onClick = {
                            onMove(statusItem.id)
                            expanded = false
                        }
                    )
                }
            if (ticket.createdBy == currentUserId) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete_ticket_option)) },
                    onClick = {
                        showConfirmDialog = true
                        expanded = false
                    }
                )
            }
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text(text = stringResource(R.string.confirm_delete_title)) },
                text = { Text(stringResource(R.string.confirm_delete_ticket_text)) },
                confirmButton = {
                    TextButton(onClick = {
                        onDelete()
                        showConfirmDialog = false
                    }) {
                        Text(stringResource(R.string.yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text(stringResource(R.string.no))
                    }
                }
            )
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

    var titleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_ticket_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (titleError) {
                            titleError = false
                        }
                    },
                    label = { Text(stringResource(R.string.title)) },
                    isError = titleError,
                    modifier = Modifier.fillMaxWidth()
                )

                if (titleError) {
                    Text(
                        text = stringResource(R.string.create_ticket_error_empty_title),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

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
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = urgExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
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
                        value = members.find { it.id == assignedToId }?.name ?: stringResource(R.string.create_ticket_placeholder_select_member),
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
                if (title.isNotBlank()) {
                    titleError = false
                    onCreate(
                        title,
                        description,
                        selectedUrgency.value,
                        assignedToId
                    )
                } else {
                    titleError = true
                }
            }) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

