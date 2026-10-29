package com.prajwalprince94.expensetracker.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Budget allocation per expense category with paise precision.
 */
data class CategoryBudgetItem(
    val id: String,
    val category: String,
    val spentPaise: Long,
    val limitPaise: Long,
    val iconName: String = "Fastfood"
) {
    val progress: Float
        get() = if (limitPaise <= 0L) 0f else (spentPaise.toFloat() / limitPaise.toFloat()).coerceAtLeast(0f)

    val remainingPaise: Long
        get() = (limitPaise - spentPaise).coerceAtLeast(0L)

    val isOverBudget: Boolean
        get() = spentPaise > limitPaise

    val icon: ImageVector
        get() = when (iconName) {
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "LocalGasStation" -> Icons.Default.LocalGasStation
            "ReceiptLong" -> Icons.Default.ReceiptLong
            "FitnessCenter" -> Icons.Default.FitnessCenter
            "School" -> Icons.Default.School
            else -> Icons.Default.Fastfood
        }

    companion object {
        fun defaultList(): List<CategoryBudgetItem> = listOf(
            CategoryBudgetItem("1", "Food & Dining", 620000L, 1000000L, "Fastfood"),
            CategoryBudgetItem("2", "Groceries", 780000L, 900000L, "ShoppingCart"),
            CategoryBudgetItem("3", "Shopping & Leisure", 540000L, 500000L, "ReceiptLong"),
            CategoryBudgetItem("4", "Fuel & Transport", 280000L, 400000L, "LocalGasStation"),
            CategoryBudgetItem("5", "Health & Fitness", 120000L, 300000L, "FitnessCenter")
        )
    }
}
