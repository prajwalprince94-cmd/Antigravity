package com.prajwalprince94.expensetracker.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class SplitMode(val displayName: String) {
    EQUAL("Equally"),
    EXACT_AMOUNTS("Exact Amounts"),
    PERCENTAGES("Percentages");

    companion object {
        fun fromString(value: String): SplitMode {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: EQUAL
        }
    }
}

/**
 * A group of people sharing expenses (e.g. Flatmates, Goa Trip, Dinner).
 */
data class SplitGroup(
    val id: String = "grp_${UUID.randomUUID()}",
    val name: String,
    val description: String = "",
    val members: List<String>, // Names, e.g. ["You", "Rahul", "Priya"]
    val colorHex: String = "#3B82F6",
    val iconKey: String = "Home",
    val createdDateMillis: Long = System.currentTimeMillis()
) {
    companion object {
        fun defaultList(): List<SplitGroup> = listOf(
            SplitGroup(
                id = "grp_flatmates",
                name = "Flat 402 Roommates",
                description = "Rent, groceries, WiFi and apartment utilities",
                members = listOf("You", "Rahul", "Priya"),
                colorHex = "#10B981",
                iconKey = "Home"
            ),
            SplitGroup(
                id = "grp_goa_trip",
                name = "Goa Vacation Trip",
                description = "Flights, villa stay, beach dinners and car rentals",
                members = listOf("You", "Rahul", "Vikram", "Sneha"),
                colorHex = "#F59E0B",
                iconKey = "TimeToLeave"
            )
        )
    }
}

/**
 * An individual bill or expense split among group members.
 */
data class SplitBillItem(
    val id: String = "sbill_${UUID.randomUUID()}",
    val groupId: String,
    val title: String,
    val totalAmountPaise: Long,
    val paidBy: String = "You",
    val payerAccount: String = "Paytm / UPI",
    val splitMode: SplitMode = SplitMode.EQUAL,
    val memberShares: Map<String, Long>, // Member Name -> Amount in Paise
    val formattedDate: String = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()),
    val category: String = "Food & Dining",
    val isSettled: Boolean = false,
    val settlementDateMillis: Long? = null
) {
    val formattedTotalAmount: String
        get() = "₹%,.2f".format(Locale.US, totalAmountPaise / 100.0)

    fun shareForMember(member: String): Long {
        return memberShares[member] ?: 0L
    }

    fun formattedShareForMember(member: String): String {
        val sharePaise = shareForMember(member)
        return "₹%,.2f".format(Locale.US, sharePaise / 100.0)
    }

    companion object {
        fun defaultList(): List<SplitBillItem> {
            val now = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
            return listOf(
                SplitBillItem(
                    id = "sbill_wifi",
                    groupId = "grp_flatmates",
                    title = "Airtel Fiber WiFi & Power",
                    totalAmountPaise = 1800_00L,
                    paidBy = "You",
                    payerAccount = "HDFC Bank",
                    splitMode = SplitMode.EQUAL,
                    memberShares = mapOf(
                        "You" to 600_00L,
                        "Rahul" to 600_00L,
                        "Priya" to 600_00L
                    ),
                    formattedDate = now,
                    category = "Rent & Bills"
                ),
                SplitBillItem(
                    id = "sbill_dinner",
                    groupId = "grp_flatmates",
                    title = "Friday BBQ Dinner",
                    totalAmountPaise = 2400_00L,
                    paidBy = "Rahul",
                    payerAccount = "Paytm / UPI",
                    splitMode = SplitMode.EQUAL,
                    memberShares = mapOf(
                        "You" to 800_00L,
                        "Rahul" to 800_00L,
                        "Priya" to 800_00L
                    ),
                    formattedDate = now,
                    category = "Food & Dining"
                )
            )
        }
    }
}

/**
 * Simplified debt settlement between two members.
 */
data class DebtTransfer(
    val fromMember: String, // Owes
    val toMember: String,   // Owed to
    val amountPaise: Long
) {
    val formattedAmount: String
        get() = "₹%,.2f".format(Locale.US, amountPaise / 100.0)
}

/**
 * Summary balances for a user or group.
 */
data class GroupBalanceSummary(
    val totalSpentPaise: Long,
    val youAreOwedPaise: Long,
    val youOwePaise: Long,
    val netBalancePaise: Long, // youAreOwed - youOwe (positive = you get back, negative = you owe)
    val memberBalances: Map<String, Long>, // Member -> Net balance in paise
    val simplifiedDebts: List<DebtTransfer>
) {
    val formattedYouAreOwed: String
        get() = "₹%,.2f".format(Locale.US, youAreOwedPaise / 100.0)

    val formattedYouOwe: String
        get() = "₹%,.2f".format(Locale.US, youOwePaise / 100.0)

    val formattedTotalSpent: String
        get() = "₹%,.2f".format(Locale.US, totalSpentPaise / 100.0)

    val formattedNetBalance: String
        get() {
            val rupees = kotlin.math.abs(netBalancePaise) / 100.0
            val prefix = if (netBalancePaise >= 0) "+₹" else "-₹"
            return "$prefix%,.2f".format(Locale.US, rupees)
        }
}
