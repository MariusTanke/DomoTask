package com.mariustanke.domotask.presentation.inventory

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mariustanke.domotask.R
import com.mariustanke.domotask.domain.enums.ProductCategory
import com.mariustanke.domotask.domain.enums.ProductUnit
import com.mariustanke.domotask.domain.model.InventoryItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InventoryItemCard(
    item: InventoryItem,
    isModified: Boolean = false,
    isNew: Boolean = false,
    isDeleted: Boolean = false,
    originalQuantity: Double? = null,
    onQuickAdd: () -> Unit,
    onQuickRemove: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit,
    onUndoDelete: () -> Unit = {}
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isLowStock = item.minQuantity > 0 && item.quantity <= item.minQuantity
    val category = ProductCategory.fromValue(item.productCategory)
    val unit = ProductUnit.fromValue(item.unit)
    val categoryColor = categoryAccentColor(category)
    val emoji = categoryEmoji(category)

    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Accent bar: category color by default, overridden by state
    val accentColor = when {
        isDeleted -> MaterialTheme.colorScheme.error
        isLowStock -> MaterialTheme.colorScheme.error
        else -> categoryColor
    }

    val contentAlpha = if (isDeleted) 0.4f else 1f

    // Stock bar
    val stockRatio = if (item.minQuantity > 0) {
        (item.quantity / (item.minQuantity * 3)).coerceIn(0.0, 1.0).toFloat()
    } else 1f

    val animatedStock by animateFloatAsState(
        targetValue = stockRatio, animationSpec = tween(500), label = "stock"
    )
    val stockBarColor by animateColorAsState(
        targetValue = when {
            isLowStock -> MaterialTheme.colorScheme.error
            stockRatio < 0.5f -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.secondary
        },
        animationSpec = tween(400), label = "stockColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { if (!isDeleted) showDeleteConfirm = true }
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor, RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 12.dp, end = 4.dp, top = 10.dp,
                            bottom = if (!isDeleted && item.minQuantity > 0) 6.dp else 10.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category emoji
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDark) categoryColor.copy(alpha = 0.15f * contentAlpha)
                                else categoryColor.copy(alpha = 0.10f * contentAlpha)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 18.sp)
                    }

                    Spacer(Modifier.width(12.dp))

                    // Product info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.productName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                            textDecoration = if (isDeleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                            )
                            if (isNew) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.inventory_pending_badge).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            if (isDeleted) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.inventory_deleted_badge).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    if (isDeleted) {
                        // Undo button for deleted items
                        TextButton(onClick = onUndoDelete) {
                            Text(
                                text = stringResource(R.string.back),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        // Right: quantity controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onQuickRemove,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = stringResource(R.string.inventory_remove_quantity),
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .widthIn(min = 40.dp)
                                    .clickable { onEditClick() }
                            ) {
                                if (isModified && originalQuantity != null && originalQuantity != item.quantity) {
                                    Text(
                                        text = formatQuantity(originalQuantity),
                                        style = MaterialTheme.typography.labelSmall,
                                        textDecoration = TextDecoration.LineThrough,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                                Text(
                                    text = formatQuantity(item.quantity),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isNew -> MaterialTheme.colorScheme.tertiary
                                        isModified -> MaterialTheme.colorScheme.primary
                                        isLowStock -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = unit.abbreviation,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = onQuickAdd,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = stringResource(R.string.inventory_add_quantity),
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Stock level bar (hidden when deleted)
                if (!isDeleted && item.minQuantity > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedStock)
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(stockBarColor)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.inventory_confirm_delete)) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick()
                    showDeleteConfirm = false
                }) {
                    Text(stringResource(R.string.yes_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

private fun formatQuantity(quantity: Double): String {
    return if (quantity == quantity.toLong().toDouble()) {
        quantity.toLong().toString()
    } else {
        "%.1f".format(quantity)
    }
}

private fun categoryEmoji(category: ProductCategory): String {
    return when (category) {
        ProductCategory.FRUIT -> "\uD83C\uDF4E"
        ProductCategory.VEGETABLE -> "\uD83E\uDD66"
        ProductCategory.MEAT -> "\uD83E\uDD69"
        ProductCategory.FISH -> "\uD83D\uDC1F"
        ProductCategory.DAIRY -> "\uD83E\uDDC0"
        ProductCategory.FROZEN -> "\u2744\uFE0F"
        ProductCategory.BEVERAGE -> "\uD83E\uDD64"
        ProductCategory.BAKERY -> "\uD83C\uDF5E"
        ProductCategory.CLEANING -> "\uD83E\uDDF9"
        ProductCategory.HYGIENE -> "\uD83E\uDDF4"
        ProductCategory.PANTRY -> "\uD83C\uDFFA"
        ProductCategory.SNACK -> "\uD83C\uDF7F"
        ProductCategory.INTERNATIONAL -> "\uD83C\uDF0D"
        ProductCategory.OTHER -> "\uD83D\uDCE6"
    }
}

private fun categoryAccentColor(category: ProductCategory): Color {
    return when (category) {
        ProductCategory.FRUIT -> Color(0xFF4CAF50)
        ProductCategory.VEGETABLE -> Color(0xFF66BB6A)
        ProductCategory.MEAT -> Color(0xFFEF5350)
        ProductCategory.FISH -> Color(0xFF42A5F5)
        ProductCategory.DAIRY -> Color(0xFFFFCA28)
        ProductCategory.FROZEN -> Color(0xFF29B6F6)
        ProductCategory.BEVERAGE -> Color(0xFF26C6DA)
        ProductCategory.BAKERY -> Color(0xFFFF7043)
        ProductCategory.CLEANING -> Color(0xFF7E57C2)
        ProductCategory.HYGIENE -> Color(0xFFEC407A)
        ProductCategory.PANTRY -> Color(0xFF8D6E63)
        ProductCategory.SNACK -> Color(0xFFFFA726)
        ProductCategory.INTERNATIONAL -> Color(0xFF5C6BC0)
        ProductCategory.OTHER -> Color(0xFF78909C)
    }
}
