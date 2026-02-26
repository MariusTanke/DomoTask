package com.mariustanke.domotask.presentation.board

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mariustanke.domotask.R
import com.mariustanke.domotask.domain.model.Ticket
import com.mariustanke.domotask.domain.model.User
import com.mariustanke.domotask.presentation.inventory.InventoryContent
import com.mariustanke.domotask.ui.theme.appGradientBackground

enum class BoardSection {
    TICKETS, MEMBERS, INVENTORY, SETTINGS
}

@Composable
fun BoardScreen(
    boardId: String,
    viewModel: BoardViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onTicketClick: (boardId: String, ticketId: String) -> Unit
) {
    val context = LocalContext.current

    var selectedFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var activeSection by rememberSaveable { mutableStateOf(BoardSection.TICKETS) }

    var showDialog by remember { mutableStateOf(false) }
    var showAddInventoryDialog by remember { mutableStateOf(false) }
    var inventoryHasPendingChanges by remember { mutableStateOf(false) }

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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            currentUserId?.let { id ->
                TopBar(
                    boardName = boardData?.name ?: "",
                    onBackClick = onBackClick,
                    boardOwnerId = boardData?.createdBy ?: "",
                    onLeaveBoard = {
                        viewModel.removeMember(boardId, id, false)
                    },
                    onDeleteBoard = {
                        viewModel.deleteBoard(boardId)
                        onBackClick()
                    },
                    currentUserId = id,
                    onFilterSelected = { selectedFilter = it },
                    members = members,
                    activeSection = activeSection,
                    onSectionSelected = { activeSection = it }
                )
            }
        },
        floatingActionButton = {
            if (activeSection == BoardSection.TICKETS) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_ticket))
                }
            } else if (activeSection == BoardSection.INVENTORY) {
                FloatingActionButton(
                    onClick = { showAddInventoryDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.inventory_add_product))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appGradientBackground())
        ) {
        AnimatedContent(
            targetState = activeSection,
            transitionSpec = {
                val direction = targetState.ordinal - initialState.ordinal
                val slideIn = if (direction > 0) { it: Int -> it / 3 } else { it: Int -> -it / 3 }
                val slideOut = if (direction > 0) { it: Int -> -it / 3 } else { it: Int -> it / 3 }
                (fadeIn(tween(250)) + slideInHorizontally(tween(300), slideIn)) togetherWith
                    (fadeOut(tween(250)) + slideOutHorizontally(tween(300), slideOut))
            },
            label = "section"
        ) { section ->
            when (section) {
                BoardSection.TICKETS -> {
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
                    }
                }

                BoardSection.MEMBERS -> {
                    MembersContent(
                        modifier = Modifier.padding(innerPadding),
                        members = members,
                        currentUserId = currentUserId ?: "",
                        boardOwnerId = boardData?.createdBy ?: "",
                        onInviteMember = { viewModel.inviteMember(boardId, it) },
                        onRemoveMember = { userId, removeTickets ->
                            viewModel.removeMember(boardId, userId, removeTickets)
                        }
                    )
                }

                BoardSection.INVENTORY -> {
                    InventoryContent(
                        modifier = Modifier.padding(innerPadding),
                        boardId = boardId,
                        showAddDialog = showAddInventoryDialog,
                        onAddDialogDismiss = { showAddInventoryDialog = false },
                        onPendingChangesChanged = { }
                    )
                }

                BoardSection.SETTINGS -> {
                    PlaceholderContent(
                        modifier = Modifier.padding(innerPadding),
                        icon = Icons.Default.Settings,
                        title = stringResource(R.string.section_settings),
                        subtitle = stringResource(R.string.coming_soon)
                    )
                }
            }
        }
        } // end gradient Box
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

}

@Composable
fun PlaceholderContent(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
