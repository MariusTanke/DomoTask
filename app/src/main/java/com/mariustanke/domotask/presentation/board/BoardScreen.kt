package com.mariustanke.domotask.presentation.board

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    boardId: String,
    boardName: String,
    viewModel: BoardViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onTicketClick: (boardId: String, ticketId: String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    // Campos del ticket
    var ticketTitle by remember { mutableStateOf("") }
    var ticketDescription by remember { mutableStateOf("") }
    val urgencyOptions = listOf("Alta", "Media", "Baja")
    var selectedUrgency by remember { mutableStateOf(urgencyOptions[1]) }
    var assignedTo by remember { mutableStateOf("") }

    val tickets by viewModel.tickets.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTickets(boardId)
    }

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
                        onClick = { onBackClick() },
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                },
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = boardName,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir ticket")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (tickets.isEmpty()) {
                Text("No hay tickets", style = MaterialTheme.typography.bodyLarge)
            } else {
                tickets.forEach { ticket ->
                    PostItTicketCard(ticket = ticket, onClick = {
                        onTicketClick(boardId, ticket.id)
                    })
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    ticketTitle = ""
                    ticketDescription = ""
                    assignedTo = ""
                },
                title = { Text("Nuevo Ticket") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = ticketTitle,
                            onValueChange = { ticketTitle = it },
                            label = { Text("Título") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ticketDescription,
                            onValueChange = { ticketDescription = it },
                            label = { Text("Descripción") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            maxLines = 5
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        var expanded by remember { mutableStateOf(false) }

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
                                urgencyOptions.forEach { urgency ->
                                    DropdownMenuItem(
                                        text = { Text(urgency) },
                                        onClick = {
                                            selectedUrgency = urgency
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = assignedTo,
                            onValueChange = { assignedTo = it },
                            label = { Text("Asignado a") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val user = viewModel.currentUser
                            if (ticketTitle.isNotBlank() && user != null) {
                                val ticket = Ticket(
                                    title = ticketTitle,
                                    description = ticketDescription,
                                    urgency = selectedUrgency,
                                    createdBy = user.uid,
                                    assignedTo = assignedTo,
                                    createdAt = System.currentTimeMillis()
                                )
                                viewModel.createTicket(boardId, ticket)
                            }
                            showDialog = false
                            ticketTitle = ""
                            ticketDescription = ""
                            assignedTo = ""
                        }
                    ) {
                        Text("Crear")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            ticketTitle = ""
                            ticketDescription = ""
                            assignedTo = ""
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
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
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
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
