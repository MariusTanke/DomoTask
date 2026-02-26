package com.mariustanke.domotask.presentation.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mariustanke.domotask.R
import com.mariustanke.domotask.domain.enums.ProductCategory
import com.mariustanke.domotask.domain.enums.ProductUnit
import com.mariustanke.domotask.domain.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInventoryItemDialog(
    searchResults: List<Product>,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onAdd: (product: Product, quantity: Double, unit: String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantityText by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf(ProductUnit.UNIT) }
    var unitExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            onClearSearch()
            onDismiss()
        },
        title = { Text(stringResource(R.string.inventory_add_product)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (selectedProduct == null) {
                    // Search phase
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            onSearch(it)
                        },
                        label = { Text(stringResource(R.string.inventory_search_hint)) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    if (searchQuery.length >= 2) {
                        if (searchResults.isEmpty()) {
                            Text(
                                text = stringResource(R.string.inventory_no_results),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                            ) {
                                items(searchResults) { product ->
                                    ProductSearchResultItem(
                                        product = product,
                                        onClick = { selectedProduct = product }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Configuration phase
                    Text(
                        text = selectedProduct!!.name,
                        style = MaterialTheme.typography.titleSmall
                    )

                    val cat = ProductCategory.fromValue(selectedProduct!!.category)
                    SuggestionChip(
                        onClick = {},
                        label = { Text(cat.label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(6.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(stringResource(R.string.inventory_quantity)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))

                    // Unit selector
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedUnit.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.inventory_unit)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            ProductUnit.entries.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text("${unit.label} (${unit.abbreviation})") },
                                    onClick = {
                                        selectedUnit = unit
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(onClick = {
                        selectedProduct = null
                        searchQuery = ""
                        onClearSearch()
                    }) {
                        Text(stringResource(R.string.back))
                    }
                }
            }
        },
        confirmButton = {
            if (selectedProduct != null) {
                Button(
                    onClick = {
                        val qty = quantityText.toDoubleOrNull() ?: 1.0
                        onAdd(selectedProduct!!, qty, selectedUnit.value)
                        onClearSearch()
                        onDismiss()
                    },
                    enabled = (quantityText.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text(stringResource(R.string.add))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onClearSearch()
                onDismiss()
            }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ProductSearchResultItem(
    product: Product,
    onClick: () -> Unit
) {
    val category = ProductCategory.fromValue(product.category)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            SuggestionChip(
                onClick = {},
                label = {
                    Text(category.label, style = MaterialTheme.typography.labelSmall)
                },
                modifier = Modifier.height(24.dp),
                shape = RoundedCornerShape(6.dp)
            )
        }
    }
}
