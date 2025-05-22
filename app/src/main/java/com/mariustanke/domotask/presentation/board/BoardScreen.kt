package com.mariustanke.domotask.presentation.board

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
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
import com.mariustanke.domotask.domain.model.Status
import com.mariustanke.domotask.domain.model.Ticket

@Composable
fun BoardScreen(
    boardId: String,
    boardName: String,
    viewModel: BoardViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onTicketClick: (boardId: String, ticketId: String) -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showMemberDialog by remember { mutableStateOf(false) }
    var inviteInput by remember { mutableStateOf("") }

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
            TopBar(
                boardName = boardName,
                showMenu = showMenu,
                onBackClick = onBackClick,
                onMemberManagementClick = {
                    showMemberDialog = true
                    showMenu = false
                },
                onMenuClick = { showMenu = true },
                onDismissMenu = { showMenu = false }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir ticket")
            }
        }
    ) { innerPadding ->
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
                    val columnTickets = tickets.filter { it.status == status.id }
                    TicketColumn(
                        status = status,
                        tickets = columnTickets,
                        allStatuses = statuses,
                        viewModel = viewModel,
                        boardId = boardId,
                        onTicketClick = onTicketClick
                    )
                }
            }

            // Crear ticket...
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
                    onDismiss = { showDialog = false }
                )
            }

            // Diálogo de Miembros
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
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = "Avatar",
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = user.name,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(onClick = {
                                                viewModel.removeMember(boardId, user.id)
                                            }) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Borrar miembro"
                                                )
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
    onMemberManagementClick: () -> Unit,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onBackClick: () -> Unit
) {
    var barWidthPx by remember { mutableStateOf(0) }
    var menuWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    val offsetDp by derivedStateOf {
        val dx = (barWidthPx - menuWidthPx).coerceAtLeast(0)
        with(density) { dx.toDp() }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .onGloballyPositioned { barWidthPx = it.size.width }
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver atrás",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            text = boardName,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "Más opciones",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onDismissMenu,
            modifier = Modifier.onGloballyPositioned { menuWidthPx = it.size.width },
            offset = DpOffset(x = offsetDp, y = 0.dp)
        ) {
            DropdownMenuItem(text = { Text("Gestionar miembros") }, onClick = onMemberManagementClick)
            Divider()
            DropdownMenuItem(text = { Text("Editar tabla") }, onClick = onDismissMenu)
            Divider()
            DropdownMenuItem(text = { Text("Añadir Estado") }, onClick = onDismissMenu)
            Divider()
            DropdownMenuItem(text = { Text("Eliminar tabla") }, onClick = onDismissMenu)
        }
    }
}

@Composable
fun TicketColumn(
    status: Status,
    tickets: List<Ticket>,
    allStatuses: List<Status>,
    viewModel: BoardViewModel,
    boardId: String,
    onTicketClick: (boardId: String, ticketId: String) -> Unit
) {
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
            Text(status.name, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            tickets.forEach { ticket ->
                PostItTicketCard(
                    ticket = ticket,
                    allStatuses = allStatuses,
                    currentUserId = viewModel.currentUser?.uid.orEmpty(),
                    onClick = { onTicketClick(boardId, ticket.id) },
                    onMove = { newStatus -> viewModel.updateTicket(boardId, ticket.copy(status = newStatus)) },
                    onDelete = { viewModel.deleteTicket(boardId, ticket.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItTicketCard(
    ticket: Ticket,
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
                .combinedClickable(onClick = onClick, onLongClick = { expanded = true }),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(ticket.title, style = MaterialTheme.typography.titleMedium)
                if (ticket.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(ticket.description, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                Text("Urgencia: ${ticket.urgency}", style = MaterialTheme.typography.labelSmall)
                Text("Asignado a: ${ticket.assignedTo}", style = MaterialTheme.typography.labelSmall)
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
                Divider()
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
    onCreate: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val urgencies = listOf("Alta", "Media", "Baja")
    var selectedUrgency by remember { mutableStateOf(urgencies[1]) }
    var assignedTo by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

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
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    maxLines = 5
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedUrgency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Urgencia") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        urgencies.forEach { urg ->
                            DropdownMenuItem(
                                text = { Text(urg) },
                                onClick = {
                                    selectedUrgency = urg
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = assignedTo,
                    onValueChange = { assignedTo = it },
                    label = { Text("Asignado a") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(title, description, selectedUrgency, assignedTo) }) {
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
