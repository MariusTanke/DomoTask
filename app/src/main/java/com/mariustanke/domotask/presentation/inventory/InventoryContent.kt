package com.mariustanke.domotask.presentation.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mariustanke.domotask.R
import com.mariustanke.domotask.domain.enums.ProductCategory
import com.mariustanke.domotask.presentation.common.categoryEmoji

@Composable
fun InventoryContent(
    modifier: Modifier = Modifier,
    boardId: String,
    showAddDialog: Boolean,
    onAddDialogDismiss: () -> Unit,
    onPendingChangesChanged: (Boolean) -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val displayItems by viewModel.displayItems.collectAsState()
    val filteredItems by viewModel.filteredItems.collectAsState()
    val pendingChanges by viewModel.pendingChanges.collectAsState()
    val hasPendingChanges by viewModel.hasPendingChanges.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val history by viewModel.history.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<com.mariustanke.domotask.domain.model.InventoryItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Derive categories present in the inventory
    val presentCategories by remember(displayItems) {
        derivedStateOf {
            displayItems.map { ProductCategory.fromValue(it.productCategory) }.distinct().sorted()
        }
    }

    LaunchedEffect(boardId) {
        viewModel.loadInventory(boardId)
    }

    LaunchedEffect(hasPendingChanges) {
        onPendingChangesChanged(hasPendingChanges)
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header row with title and history button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.section_inventory),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.inventory_search_items_hint)
                    )
                }
                IconButton(onClick = { showHistory = true }) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = stringResource(R.string.inventory_history)
                    )
                }
            }

            // Collapsible search, sort & filter section
            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text(stringResource(R.string.inventory_search_items_hint)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.inventory_clear_search)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    // Sort + Category filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SortDropdownButton(
                            currentSort = sortOption,
                            onSortSelected = { viewModel.setSortOption(it) }
                        )

                        // Vertical divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )

                        presentCategories.forEach { category ->
                            val selected = category in selectedCategories
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.toggleCategory(category) },
                                label = {
                                    Text("${categoryEmoji(category)} ${category.label}")
                                }
                            )
                        }

                        if (selectedCategories.isNotEmpty()) {
                            AssistChip(
                                onClick = { viewModel.clearCategoryFilters() },
                                label = { Text(stringResource(R.string.inventory_clear_filters)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                }
            }

            if (isLoading || isSaving) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            when {
                displayItems.isEmpty() && !isLoading -> {
                    // Empty inventory state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.inventory_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                filteredItems.isEmpty() && displayItems.isNotEmpty() -> {
                    // No results for current filters
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.inventory_no_results_filter),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = if (hasPendingChanges) 110.dp else 80.dp)
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            val change = pendingChanges[item.id]
                            InventoryItemCard(
                                item = item,
                                isModified = change != null && !change.isNew && !change.isDeleted,
                                isNew = change?.isNew == true,
                                isDeleted = change?.isDeleted == true,
                                originalQuantity = change?.originalQuantity,
                                onQuickAdd = { viewModel.updateLocalQuantity(item.id, 1.0) },
                                onQuickRemove = { viewModel.updateLocalQuantity(item.id, -1.0) },
                                onEditClick = { editingItem = item },
                                onDeleteClick = { viewModel.deleteItem(item.id) },
                                onUndoDelete = { viewModel.undoDeleteItem(item.id) }
                            )
                        }
                    }
                }
            }
        }

        // Bottom bar for pending changes
        AnimatedVisibility(
            visible = hasPendingChanges,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 80.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pending count badge
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$pendingCount",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.inventory_changes_count, pendingCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    // Discard button
                    FilledTonalIconButton(
                        onClick = { viewModel.discardChanges() },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.inventory_discard_changes),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    // Save button
                    FilledIconButton(
                        onClick = { showSaveDialog = true },
                        enabled = !isSaving,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = stringResource(R.string.save),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (hasPendingChanges) 64.dp else 0.dp)
        )
    }

    // Add dialog — adds locally, not to Firestore
    if (showAddDialog) {
        AddInventoryItemDialog(
            searchResults = searchResults,
            onSearch = { viewModel.searchProducts(it) },
            onClearSearch = { viewModel.clearSearch() },
            onAdd = { product, quantity, unit ->
                viewModel.addNewProduct(product, quantity, unit)
            },
            onDismiss = onAddDialogDismiss
        )
    }

    // Save dialog with optional comment
    if (showSaveDialog) {
        SaveChangesDialog(
            onConfirm = { comment ->
                viewModel.saveAll(comment)
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    // History sheet
    if (showHistory) {
        InventoryHistorySheet(
            history = history,
            onDismiss = { showHistory = false }
        )
    }

    // Edit quantity/unit dialog
    editingItem?.let { item ->
        EditQuantityDialog(
            currentQuantity = item.quantity,
            currentUnit = item.unit,
            productName = item.productName,
            onConfirm = { qty, unit ->
                viewModel.setLocalQuantityAndUnit(item.id, qty, unit)
                editingItem = null
            },
            onDismiss = { editingItem = null }
        )
    }
}

@Composable
private fun SortDropdownButton(
    currentSort: InventorySortOption,
    onSortSelected: (InventorySortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(currentSort.label) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(R.string.inventory_sort),
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            InventorySortOption.entries.forEach { option ->
                val isSelected = option == currentSort
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSortSelected(option)
                        expanded = false
                    },
                    trailingIcon = {
                        if (isSelected) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SaveChangesDialog(
    onConfirm: (comment: String) -> Unit,
    onDismiss: () -> Unit
) {
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.inventory_save_title)) },
        text = {
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text(stringResource(R.string.inventory_save_comment_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(comment) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
