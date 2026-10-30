package com.prajwalprince94.expensetracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Centralized Icon & Palette registry for dynamic category and account visual styling.
 */
object IconRegistry {

    private val iconMap: Map<String, ImageVector> = mapOf(
        "Fastfood" to Icons.Default.Fastfood,
        "ShoppingCart" to Icons.Default.ShoppingCart,
        "LocalGasStation" to Icons.Default.LocalGasStation,
        "Home" to Icons.Default.Home,
        "ReceiptLong" to Icons.Default.ReceiptLong,
        "FitnessCenter" to Icons.Default.FitnessCenter,
        "School" to Icons.Default.School,
        "CardTravel" to Icons.Default.CardTravel,
        "Work" to Icons.Default.Work,
        "Payments" to Icons.Default.Payments,
        "AccountBalance" to Icons.Default.AccountBalance,
        "LocalAtm" to Icons.Default.LocalAtm,
        "PhoneAndroid" to Icons.Default.PhoneAndroid,
        "CreditCard" to Icons.Default.CreditCard,
        "Savings" to Icons.Default.Savings,
        "Flight" to Icons.Default.Flight,
        "DirectionsCar" to Icons.Default.DirectionsCar,
        "Movie" to Icons.Default.Movie,
        "Pets" to Icons.Default.Pets,
        "Coffee" to Icons.Default.Coffee,
        "ShoppingBag" to Icons.Default.ShoppingBag,
        "AccountBalanceWallet" to Icons.Default.AccountBalanceWallet,
        "Category" to Icons.Default.Category
    )

    /**
     * Resolves an iconKey string to its corresponding ImageVector with a safe fallback.
     */
    fun getIcon(key: String): ImageVector {
        return iconMap[key] ?: Icons.Default.Category
    }

    /**
     * Curated list of available icons for category and account picker selectors.
     */
    val availableIcons: List<Pair<String, ImageVector>> = listOf(
        "Fastfood" to Icons.Default.Fastfood,
        "Coffee" to Icons.Default.Coffee,
        "ShoppingCart" to Icons.Default.ShoppingCart,
        "ShoppingBag" to Icons.Default.ShoppingBag,
        "ReceiptLong" to Icons.Default.ReceiptLong,
        "LocalGasStation" to Icons.Default.LocalGasStation,
        "DirectionsCar" to Icons.Default.DirectionsCar,
        "Flight" to Icons.Default.Flight,
        "CardTravel" to Icons.Default.CardTravel,
        "Home" to Icons.Default.Home,
        "FitnessCenter" to Icons.Default.FitnessCenter,
        "School" to Icons.Default.School,
        "PhoneAndroid" to Icons.Default.PhoneAndroid,
        "AccountBalance" to Icons.Default.AccountBalance,
        "Payments" to Icons.Default.Payments,
        "CreditCard" to Icons.Default.CreditCard,
        "Savings" to Icons.Default.Savings,
        "LocalAtm" to Icons.Default.LocalAtm,
        "Work" to Icons.Default.Work,
        "Movie" to Icons.Default.Movie,
        "Pets" to Icons.Default.Pets
    )

    /**
     * Curated expressive color swatches for tags, badges, and account cards.
     */
    val availableColorHexes: List<String> = listOf(
        "#10B981", // Emerald
        "#0EA5E9", // Sky Blue
        "#6366F1", // Indigo
        "#8B5CF6", // Purple
        "#F43F5E", // Rose Red
        "#EF4444", // Red
        "#F59E0B", // Amber
        "#06B6D4", // Cyan
        "#EC4899", // Pink
        "#14B8A6"  // Teal
    )

    /**
     * Converts a hex color string (e.g. "#10B981") to Compose Color with fallback.
     */
    fun parseColor(hex: String, fallback: Color = Color(0xFF10B981)): Color {
        return try {
            val clean = hex.removePrefix("#")
            val colorInt = if (clean.length == 6) {
                ("FF$clean").toLong(16).toInt()
            } else {
                clean.toLong(16).toInt()
            }
            Color(colorInt)
        } catch (e: Exception) {
            fallback
        }
    }
}
