package com.mariustanke.domotask.presentation.board

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mariustanke.domotask.R
import com.mariustanke.domotask.domain.model.Status
import com.mariustanke.domotask.domain.model.Ticket
import com.mariustanke.domotask.domain.model.User

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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
        )
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
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                status.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
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
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        status.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

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

internal fun initials(name: String): String {
    return name
        .split(" ")
        .filter { it.isNotBlank() }
        .map { it.first().uppercaseChar() }
        .take(2)
        .joinToString("")
}
