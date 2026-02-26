package com.mariustanke.domotask.presentation.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mariustanke.domotask.R
import com.mariustanke.domotask.domain.enums.UrgencyEnum
import com.mariustanke.domotask.domain.model.User

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
