package com.mariustanke.domotask.presentation.common

import androidx.compose.ui.graphics.Color
import com.mariustanke.domotask.domain.enums.ProductCategory

fun categoryEmoji(category: ProductCategory): String {
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

fun categoryAccentColor(category: ProductCategory): Color {
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
