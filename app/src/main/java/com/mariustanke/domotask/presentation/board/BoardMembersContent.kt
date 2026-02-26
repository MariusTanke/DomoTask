package com.mariustanke.domotask.presentation.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.mariustanke.domotask.domain.model.User

@Composable
fun MembersContent(
    modifier: Modifier = Modifier,
    members: List<User>,
    currentUserId: String,
    boardOwnerId: String,
    onInviteMember: (String) -> Unit,
    onRemoveMember: (userId: String, removeTickets: Boolean) -> Unit
) {
    var inviteInput by remember { mutableStateOf("") }
    var memberToDelete by remember { mutableStateOf<User?>(null) }
    var removeUserTickets by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.members_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        members.forEach { user ->
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    if (user.photo != null) {
                        AsyncImage(
                            model = user.photo,
                            contentDescription = stringResource(R.string.cd_avatar_of, user.name),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            placeholder = painterResource(R.drawable.placeholder_avatar),
                            error = painterResource(R.drawable.placeholder_avatar),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials(user.name),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (user.id == boardOwnerId) {
                            Text(
                                text = stringResource(R.string.label_owner),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (currentUserId == boardOwnerId && user.id != currentUserId) {
                        IconButton(onClick = {
                            memberToDelete = user
                            removeUserTickets = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.remove_member_label),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        if (currentUserId == boardOwnerId) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.invite_member_label),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = inviteInput,
                    onValueChange = { inviteInput = it },
                    label = { Text(stringResource(R.string.invite_member_label)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    onInviteMember(inviteInput)
                    inviteInput = ""
                }) {
                    Text(stringResource(R.string.invite_member_confirm))
                }
            }
        }
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
                    onRemoveMember(user.id, removeUserTickets)
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
