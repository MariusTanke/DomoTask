package com.mariustanke.domotask.presentation.board

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp

import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.mariustanke.domotask.R
import com.mariustanke.domotask.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    boardName: String,
    members: List<User>,
    onBackClick: () -> Unit,
    currentUserId: String,
    boardOwnerId: String,
    onLeaveBoard: () -> Unit,
    onDeleteBoard: () -> Unit,
    onFilterSelected: (String?) -> Unit,
    activeSection: BoardSection,
    onSectionSelected: (BoardSection) -> Unit,
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    var showConfirmDeleteBoardDialog by remember { mutableStateOf(false) }
    var showConfirmLeaveDialog by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.primary)
    ) {
        TopAppBar(
            windowInsets = WindowInsets(0, 0, 0, 0),
            title = {
                Text(
                    text = boardName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            actions = {
                if (activeSection == BoardSection.TICKETS) {
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_filter),
                                contentDescription = stringResource(R.string.filter),
                            )
                        }

                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
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
                    }
                }
            }
        )
        AnimatedVisibility(
            visible = showOptions,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SectionIcon(
                    icon = Icons.AutoMirrored.Filled.List,
                    contentDescription = stringResource(R.string.section_tickets),
                    isSelected = activeSection == BoardSection.TICKETS,
                    onClick = { onSectionSelected(BoardSection.TICKETS) }
                )
                SectionIcon(
                    icon = Icons.Default.Person,
                    contentDescription = stringResource(R.string.section_members),
                    isSelected = activeSection == BoardSection.MEMBERS,
                    onClick = { onSectionSelected(BoardSection.MEMBERS) }
                )
                SectionIcon(
                    icon = Icons.Default.ShoppingCart,
                    contentDescription = stringResource(R.string.section_inventory),
                    isSelected = activeSection == BoardSection.INVENTORY,
                    onClick = { onSectionSelected(BoardSection.INVENTORY) }
                )
                SectionIcon(
                    icon = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.section_settings),
                    isSelected = activeSection == BoardSection.SETTINGS,
                    onClick = { onSectionSelected(BoardSection.SETTINGS) }
                )
                SectionIcon(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = if (currentUserId == boardOwnerId)
                        stringResource(R.string.menu_delete_board)
                    else stringResource(R.string.menu_leave_board),
                    isSelected = false,
                    onClick = {
                        if (currentUserId == boardOwnerId) {
                            showConfirmDeleteBoardDialog = true
                        } else {
                            showConfirmLeaveDialog = true
                        }
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showOptions = !showOptions },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (showOptions) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
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
private fun SectionIcon(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .then(
                    if (isSelected) Modifier.background(
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                        CircleShape
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            )
        }
    }
}
