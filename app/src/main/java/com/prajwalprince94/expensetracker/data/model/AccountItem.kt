package com.prajwalprince94.expensetracker.data.model

import java.util.Locale

/**
 * Represents a financial account (Bank, UPI Wallet, Physical Cash, Credit Card).
 * Tracks opening balance and account styling.
 */
data class AccountItem(
    val id: String,
    val name: String,
    val type: String, // "UPI", "BANK", "CASH", "CARD", "SAVINGS"
    val initialBalancePaise: Long,
    val colorHex: String = "#0EA5E9",
    val iconKey: String = "PhoneAndroid"
) {
    val formattedInitialBalance: String
        get() = "₹%,.2f".format(Locale.US, initialBalancePaise / 100.0)

    companion object {
        fun defaultList(): List<AccountItem> = listOf(
            AccountItem(
                id = "acc_upi",
                name = "Paytm / UPI",
                type = "UPI",
                initialBalancePaise = 15_000_00L,
                colorHex = "#0EA5E9",
                iconKey = "PhoneAndroid"
            ),
            AccountItem(
                id = "acc_hdfc",
                name = "HDFC Bank",
                type = "BANK",
                initialBalancePaise = 45_000_00L,
                colorHex = "#6366F1",
                iconKey = "AccountBalance"
            ),
            AccountItem(
                id = "acc_cash",
                name = "Cash Wallet",
                type = "CASH",
                initialBalancePaise = 5_000_00L,
                colorHex = "#10B981",
                iconKey = "Payments"
            )
        )
    }
}
