package com.mariustanke.domotask.presentation.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mariustanke.domotask.R
import com.mariustanke.domotask.domain.enums.ProductUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuantityDialog(
    currentQuantity: Double,
    currentUnit: String,
    productName: String,
    onConfirm: (quantity: Double, unit: String) -> Unit,
    onDismiss: () -> Unit
) {
    val unit = ProductUnit.fromValue(currentUnit)
    var quantityText by remember { mutableStateOf(formatQtyForEdit(currentQuantity)) }
    var selectedUnit by remember { mutableStateOf(unit) }
    var unitExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(productName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text(stringResource(R.string.inventory_quantity)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        ProductUnit.entries.forEach { pu ->
                            DropdownMenuItem(
                                text = { Text("${pu.label} (${pu.abbreviation})") },
                                onClick = {
                                    selectedUnit = pu
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toDoubleOrNull() ?: currentQuantity
                    onConfirm(qty, selectedUnit.value)
                }
            ) {
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

private fun formatQtyForEdit(quantity: Double): String {
    return if (quantity == quantity.toLong().toDouble()) {
        quantity.toLong().toString()
    } else {
        "%.1f".format(quantity)
    }
}
