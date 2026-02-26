package com.mariustanke.domotask.presentation.board

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mariustanke.domotask.R
import com.mariustanke.domotask.domain.model.Status
import com.mariustanke.domotask.domain.model.Ticket
import com.mariustanke.domotask.ui.theme.*

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
    var showConfirmDialog by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val urgencyColor = when (ticket.urgency) {
        1 -> if (isDark) UrgencyGreenDark else UrgencyGreenLight
        2 -> if (isDark) UrgencyBlueDark else UrgencyBlueLight
        3 -> if (isDark) UrgencyAmberDark else UrgencyAmberLight
        4 -> if (isDark) UrgencyOrangeDark else UrgencyOrangeLight
        5 -> if (isDark) UrgencyRedDark else UrgencyRedLight
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }

    val urgencyAccent = when (ticket.urgency) {
        1 -> UrgencyGreenText
        2 -> UrgencyBlueText
        3 -> UrgencyAmberText
        4 -> UrgencyOrangeText
        5 -> UrgencyRedText
        else -> MaterialTheme.colorScheme.tertiary
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
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = urgencyColor),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(urgencyAccent, RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 12.dp,
                            end = 12.dp,
                            top = 8.dp,
                            bottom = 12.dp
                        )
                ) {
                    Text(
                        text = ticket.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    if (ticket.description.isNotBlank()) {
                        Text(
                            text = ticket.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            } // Row
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
