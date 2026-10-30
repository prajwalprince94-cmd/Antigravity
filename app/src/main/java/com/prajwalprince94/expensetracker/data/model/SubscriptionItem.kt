package com.prajwalprince94.expensetracker.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Supported recurring intervals for subscription and bill payments.
 */
enum class BillingCycle(val displayName: String, val intervalMonths: Int) {
    MONTHLY("Monthly", 1),
    QUARTERLY("Quarterly", 3),
    YEARLY("Yearly", 12);

    companion object {
        fun fromString(value: String): BillingCycle {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: MONTHLY
        }
    }
}

/**
 * Models a recurring subscription or scheduled utility bill.
 */
data class SubscriptionItem(
    val id: String,
    val name: String,
    val amountPaise: Long,
    val category: String,
    val account: String,
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val dueDay: Int, // 1 to 31
    val startDateMillis: Long = System.currentTimeMillis(),
    val nextDueDateMillis: Long = SubscriptionItem.calculateNextDueDate(dueDay, billingCycle),
    val isActive: Boolean = true,
    val autoLog: Boolean = false,
    val lastLoggedDateMillis: Long? = null,
    val colorHex: String = "#EF4444",
    val iconKey: String = "Movie"
) {
    val formattedAmount: String
        get() = "₹%,.2f".format(Locale.US, amountPaise / 100.0)

    val formattedNextDue: String
        get() {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
            return sdf.format(Date(nextDueDateMillis))
        }

    val daysUntilDue: Int
        get() {
            val now = System.currentTimeMillis()
            val diff = nextDueDateMillis - now
            return (diff / (1000 * 60 * 60 * 24)).toInt()
        }

    val isOverdue: Boolean
        get() = daysUntilDue < 0 && isActive

    companion object {
        fun calculateNextDueDate(dueDay: Int, cycle: BillingCycle = BillingCycle.MONTHLY): Long {
            val cal = Calendar.getInstance()
            val maxDayThisMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val clampedDueDay = dueDay.coerceIn(1, maxDayThisMonth)

            cal.set(Calendar.DAY_OF_MONTH, clampedDueDay)
            cal.set(Calendar.HOUR_OF_DAY, 9)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            if (cal.timeInMillis <= System.currentTimeMillis()) {
                cal.add(Calendar.MONTH, cycle.intervalMonths)
                val maxDayNextMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, dueDay.coerceIn(1, maxDayNextMonth))
            }
            return cal.timeInMillis
        }

        fun defaultList(): List<SubscriptionItem> = listOf(
            SubscriptionItem(
                id = "sub_netflix",
                name = "Netflix Premium 4K",
                amountPaise = 649_00L,
                category = "Rent & Bills",
                account = "Paytm / UPI",
                billingCycle = BillingCycle.MONTHLY,
                dueDay = 5,
                colorHex = "#F43F5E",
                iconKey = "Movie"
            ),
            SubscriptionItem(
                id = "sub_wifi",
                name = "Airtel Fiber Broadband",
                amountPaise = 999_00L,
                category = "Rent & Bills",
                account = "HDFC Bank",
                billingCycle = BillingCycle.MONTHLY,
                dueDay = 10,
                colorHex = "#0EA5E9",
                iconKey = "Home"
            ),
            SubscriptionItem(
                id = "sub_spotify",
                name = "Spotify Duo",
                amountPaise = 149_00L,
                category = "Shopping",
                account = "Paytm / UPI",
                billingCycle = BillingCycle.MONTHLY,
                dueDay = 20,
                colorHex = "#10B981",
                iconKey = "PhoneAndroid"
            )
        )
    }
}
