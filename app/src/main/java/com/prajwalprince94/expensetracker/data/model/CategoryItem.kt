package com.prajwalprince94.expensetracker.data.model

import com.prajwalprince94.expensetracker.ui.model.TransactionType

/**
 * Represents an expense or income classification category.
 * Supports system default categories and user-created custom categories.
 */
data class CategoryItem(
    val id: String,
    val name: String,
    val type: TransactionType,
    val colorHex: String,
    val iconKey: String,
    val isSystem: Boolean = false
) {
    companion object {
        fun defaultList(): List<CategoryItem> = listOf(
            // Expense Categories
            CategoryItem("cat_food", "Food & Dining", TransactionType.EXPENSE, "#F43F5E", "Fastfood", isSystem = true),
            CategoryItem("cat_groceries", "Groceries", TransactionType.EXPENSE, "#10B981", "ShoppingCart", isSystem = true),
            CategoryItem("cat_fuel", "Fuel & Travel", TransactionType.EXPENSE, "#F59E0B", "LocalGasStation", isSystem = true),
            CategoryItem("cat_rent", "Rent & Bills", TransactionType.EXPENSE, "#8B5CF6", "Home", isSystem = true),
            CategoryItem("cat_shopping", "Shopping", TransactionType.EXPENSE, "#EC4899", "ReceiptLong", isSystem = true),
            CategoryItem("cat_health", "Health & Fit", TransactionType.EXPENSE, "#06B6D4", "FitnessCenter", isSystem = true),
            CategoryItem("cat_education", "Education", TransactionType.EXPENSE, "#3B82F6", "School", isSystem = true),
            CategoryItem("cat_vacation", "Vacation", TransactionType.EXPENSE, "#14B8A6", "CardTravel", isSystem = true),

            // Income Categories
            CategoryItem("cat_salary", "Salary", TransactionType.INCOME, "#10B981", "Work", isSystem = true),
            CategoryItem("cat_freelance", "Freelance", TransactionType.INCOME, "#06B6D4", "Payments", isSystem = true),
            CategoryItem("cat_investments", "Investments", TransactionType.INCOME, "#8B5CF6", "AccountBalance", isSystem = true),
            CategoryItem("cat_gifts", "Gifts", TransactionType.INCOME, "#F59E0B", "LocalAtm", isSystem = true)
        )
    }
}
