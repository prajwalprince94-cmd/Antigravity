package com.prajwalprince94.expensetracker.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TimeToLeave
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

data class TransactionItem(
    val id: String,
    val title: String,
    val category: String,
    val account: String,
    val amountPaise: Long,
    val type: TransactionType,
    val formattedDate: String,
    val note: String = "",
    val icon: ImageVector = Icons.Default.ReceiptLong
) {
    val formattedAmount: String
        get() {
            val rupees = amountPaise / 100.0
            val prefix = when (type) {
                TransactionType.EXPENSE -> "-₹"
                TransactionType.INCOME -> "+₹"
                TransactionType.TRANSFER -> "₹"
            }
            return "$prefix%,.2f".format(rupees)
        }

    companion object {
        fun defaultSampleList(): List<TransactionItem> = listOf(
            TransactionItem(
                id = "1",
                title = "Grocery Store",
                category = "Groceries",
                account = "Paytm / UPI",
                amountPaise = 124000L,
                type = TransactionType.EXPENSE,
                formattedDate = "Today, 4:15 PM",
                note = "Vegetables, milk, and bread",
                icon = Icons.Default.ShoppingCart
            ),
            TransactionItem(
                id = "2",
                title = "Monthly Salary",
                category = "Salary",
                account = "HDFC Bank",
                amountPaise = 5000000L,
                type = TransactionType.INCOME,
                formattedDate = "Yesterday, 10:00 AM",
                note = "October salary credited",
                icon = Icons.Default.Work
            ),
            TransactionItem(
                id = "3",
                title = "Bank to Cash Withdrawal",
                category = "Transfer",
                account = "HDFC Bank ➔ Cash",
                amountPaise = 500000L,
                type = TransactionType.TRANSFER,
                formattedDate = "28 Oct, 6:30 PM",
                note = "ATM cash withdrawal for weekend",
                icon = Icons.Default.SwapHoriz
            ),
            TransactionItem(
                id = "4",
                title = "Dinner with Friends",
                category = "Food & Dining",
                account = "Cash",
                amountPaise = 75000L,
                type = TransactionType.EXPENSE,
                formattedDate = "27 Oct, 9:20 PM",
                note = "Pizza and beverages",
                icon = Icons.Default.Fastfood
            ),
            TransactionItem(
                id = "5",
                title = "Fuel & Petrol",
                category = "Transportation",
                account = "Paytm / UPI",
                amountPaise = 50000L,
                type = TransactionType.EXPENSE,
                formattedDate = "26 Oct, 8:10 AM",
                note = "Full tank petrol",
                icon = Icons.Default.TimeToLeave
            )
        )
    }
}
