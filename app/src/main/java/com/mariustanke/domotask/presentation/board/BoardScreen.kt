package com.mariustanke.domotask.presentation.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mariustanke.domotask.domain.model.Ticket

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Horizontal
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Top
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.only

@Composable
fun BoardScreen(
    boardId: String,
    boardName: String,
    viewModel: BoardViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onTicketClick: (boardId: String, ticketId: String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val tickets by viewModel.tickets.collectAsState()
    LaunchedEffect(boardId) { viewModel.loadTickets(boardId) }

    Scaffold(
        // ← Sólo mantenemos top inset, quitamos bottom
        contentWindowInsets = WindowInsets.systemBars.only(Top + Horizontal),
        topBar = {
            // Header con título perfectamente centrado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
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
                Spacer(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterEnd)
                )
            }
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
                // paddings sólo para statusBar+topBar y sides, no bottom nav
                .padding(innerPadding)
                // dejamos 8dp arriba y abajo como margen extra
                .padding(top = 8.dp, bottom = 8.dp)
        ) {
            if (tickets.isEmpty()) {
                Box(Modifier.fillMaxSize()) {
                    Text(
                        "No hay tickets",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                val scrollState = rememberScrollState()
                val todo = tickets.filter { it.status == "todo" }
                val doing = tickets.filter { it.status == "doing" }
                val done = tickets.filter { it.status == "done" }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)                     // ocupa toda la altura disponible
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TicketColumn("Por hacer", todo, boardId, onTicketClick)
                    TicketColumn("Haciendo", doing, boardId, onTicketClick)
                    TicketColumn("Hecho", done, boardId, onTicketClick)
                }
            }

            if (showDialog) {
                CreateTicketDialog(
                    onCreate = { title, desc, urg, assigned ->
                        viewModel.createTicket(
                            boardId,
                            Ticket(
                                title       = title,
                                description = desc,
                                urgency     = urg,
                                createdBy   = viewModel.currentUser?.uid.orEmpty(),
                                assignedTo  = assigned,
                                createdAt   = System.currentTimeMillis(),
                                status      = "todo"
                            )
                        )
                        showDialog = false
                    },
                    onDismiss = { showDialog = false }
                )
            }
        }
    }
}


@Composable
fun TicketColumn(
    title: String,
    tickets: List<Ticket>,
    boardId: String,
    onTicketClick: (boardId: String, ticketId: String) -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            tickets.forEach { ticket ->
                PostItTicketCard(ticket = ticket) {
                    onTicketClick(boardId, ticket.id)
                }
            }
        }
    }
}

@Composable
fun PostItTicketCard(
    ticket: Ticket,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(ticket.title, style = MaterialTheme.typography.titleMedium)
            if (ticket.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(ticket.description, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Urgencia: ${ticket.urgency}", style = MaterialTheme.typography.labelSmall)
            Text("Asignado a: ${ticket.assignedTo}", style = MaterialTheme.typography.labelSmall)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
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
            TextButton(onClick = {
                onCreate(title, description, selectedUrgency, assignedTo)
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