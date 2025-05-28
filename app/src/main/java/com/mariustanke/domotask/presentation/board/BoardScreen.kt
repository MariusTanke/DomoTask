package com.mariustanke.domotask.presentation.board

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
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

    val boardData by viewModel.board.collectAsState()
    val tickets by viewModel.tickets.collectAsState()
    val statuses by viewModel.statuses.collectAsState()
    val members by viewModel.members.collectAsState()
    val inviteState by viewModel.inviteState.collectAsState()

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
            viewModel.currentUser?.uid?.let {
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
                            viewModel.currentUser?.uid.orEmpty()
                        )
                    },
                    currentUserId = it,
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
                Icon(Icons.Default.Add, contentDescription = "Añadir ticket")
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
                    boardData?.let {
                        TicketColumn(
                            status = status,
                            members = members,
                            tickets = columnTickets,
                            allStatuses = statuses,
                            viewModel = viewModel,
                            boardId = boardId,
                            onTicketClick = onTicketClick,
                            boardOwnerId = it.createdBy
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
                                createdBy = viewModel.currentUser?.uid.orEmpty(),
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
                    title = { Text("Añadir Estado") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newStatusName,
                                onValueChange = { newStatusName = it },
                                label = { Text("Nombre del estado") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newStatusOrder.toString(),
                                onValueChange = {
                                    newStatusOrder = it.toIntOrNull() ?: newStatusOrder
                                },
                                label = { Text("Orden") },
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
                        }) { Text("Añadir") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddStatusDialog = false }) { Text("Cancelar") }
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
                    title = { Text("Miembros del tablero") },
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
                                                    contentDescription = "Avatar de ${user.name}",
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
                                                    contentDescription = "Avatar",
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
                                                if (user.id != viewModel.currentUser?.uid) {
                                                    IconButton(onClick = {
                                                        viewModel.removeMember(boardId, user.id)
                                                    }) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Borrar miembro",
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
                                label = { Text("Invitar miembro") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.inviteMember(boardId, inviteInput)
                            inviteInput = ""
                        }) {
                            Text("Invitar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showMemberDialog = false }) {
                            Text("Cancelar")
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .onGloballyPositioned { barWidthPx = it.size.width }
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver atrás",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text(
            boardName,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
            onClick = { showFilterMenu = true },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 48.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Filtrar", tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        DropdownMenu(
            expanded = showFilterMenu,
            onDismissRequest = { showFilterMenu = false },
            offset = DpOffset(x = (-40).dp, y = 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Todos") },
                onClick = {
                    onFilterSelected(null)
                    showFilterMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Sin asignar") },
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

        IconButton(onClick = onMenuClick, modifier = Modifier.align(Alignment.CenterEnd)) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Más opciones",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onDismissMenu,
            offset = DpOffset(x = offsetDp, y = 0.dp),
            modifier = Modifier.onGloballyPositioned { menuWidthPx = it.size.width }
        ) {
            if (currentUserId == boardOwnerId) {
                DropdownMenuItem(
                    text = { Text("Gestionar miembros") },
                    onClick = onMemberManagementClick
                )
                HorizontalDivider()
                DropdownMenuItem(text = { Text("Editar tabla") }, onClick = onDismissMenu)
                HorizontalDivider()
                DropdownMenuItem(text = { Text("Añadir Estado") }, onClick = onAddStatusClick)
                HorizontalDivider()
                DropdownMenuItem(text = { Text("Eliminar tabla") }, onClick = onDismissMenu)
            } else {
                DropdownMenuItem(text = { Text("Salir de la tabla") }, onClick = {
                    onLeaveBoard()
                    onBackClick()
                })
            }
        }
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
                            label = { Text("Nombre") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = editOrder.toString(),
                            onValueChange = { editOrder = it.toIntOrNull() ?: editOrder },
                            label = { Text("Orden") },
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
                                Icon(Icons.Default.Check, contentDescription = "Guardar")
                            }
                            IconButton(onClick = {
                                editName = status.name
                                editOrder = status.order
                                editing = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancelar")
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
                            Icon(Icons.Default.Edit, contentDescription = "Editar columna")
                        }
                        if (tickets.isEmpty()) {
                            IconButton(onClick = {
                                viewModel.removeBoardStatus(boardId, status.id)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar columna")
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
                val assignedUser = members.find { it.id == ticket.assignedTo}
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
}

fun initials(name: String): String {
    return name
        .split(" ")
        .filter { it.isNotBlank() }
        .map { it.first().uppercaseChar() }
        .take(2)
        .joinToString("")
}

@OptIn(ExperimentalFoundationApi::class)
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(ticket.title, style = MaterialTheme.typography.titleMedium)
                if (ticket.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(ticket.description, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(8.dp))
                val urgText = UrgencyEnum.entries
                    .find { it.value == ticket.urgency }
                    ?.label ?: "Desconocida"

                Text("Urgencia: $urgText", style = MaterialTheme.typography.labelSmall)

                if (assignedToName.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(Modifier.width(8.dp).weight(1f))
                        if (!assignedToPhoto.isNullOrBlank()) {
                            AsyncImage(
                                model = assignedToPhoto,
                                contentDescription = "Avatar de $assignedToName",
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

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            allStatuses
                .filter { it.id != ticket.status }
                .sortedBy { it.order }
                .forEach { statusItem ->
                    DropdownMenuItem(
                        text = { Text("Mover a ${statusItem.name}") },
                        onClick = {
                            onMove(statusItem.id)
                            expanded = false
                        }
                    )
                }
            if (ticket.createdBy == currentUserId) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Borrar ticket") },
                    onClick = {
                        onDelete()
                        expanded = false
                    }
                )
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
        title = { Text("Nuevo Ticket") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
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
                        label = { Text("Urgencia") },
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
                        value = members.find { it.id == assignedToId }?.name ?: "Selecciona miembro",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Asignado a") },
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
                                            contentDescription = "Foto de ${member.name}",
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
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
