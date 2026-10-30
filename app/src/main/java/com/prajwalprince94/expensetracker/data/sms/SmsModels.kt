package com.prajwalprince94.expensetracker.data.sms

import com.prajwalprince94.expensetracker.ui.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Clean data model representing an automated financial transaction extracted from SMS.
 */
data class ParsedSmsExpense(
    val id: String = "sms_${UUID.randomUUID()}",
    val sender: String,
    val bankName: String,
    val amountPaise: Long,
    val type: TransactionType = TransactionType.EXPENSE,
    val accountLast4: String? = null,
    val merchantOrBeneficiary: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val formattedDate: String = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(dateMillis)),
    val rawBody: String,
    val suggestedCategory: String = "Food & Dining",
    val suggestedAccount: String = "Paytm / UPI",
    val refNumber: String? = null,
    val isAlreadyLogged: Boolean = false
) {
    val formattedAmount: String
        get() = "₹%,.2f".format(Locale.US, amountPaise / 100.0)

    val displayAccount: String
        get() = if (!accountLast4.isNullOrBlank()) "$bankName (..$accountLast4)" else bankName
}

/**
 * Realistic Indian bank SMS presets for instant simulator testing and emulator support.
 */
data class SmsTemplatePreset(
    val id: String,
    val bankName: String,
    val title: String,
    val sender: String,
    val body: String
) {
    companion object {
        fun defaultPresets(): List<SmsTemplatePreset> = listOf(
            SmsTemplatePreset(
                id = "sms_hdfc_swiggy",
                bankName = "HDFC Bank",
                title = "Swiggy UPI Payment",
                sender = "VK-HDFCBK",
                body = "Dear Customer, Rs 349.00 debited from a/c **4829 on 30-10-26 to Swiggy UPI. UPI Ref 630198421092. Avl bal: Rs 14,820.50. Call 18002583838 if not you."
            ),
            SmsTemplatePreset(
                id = "sms_sbi_atm",
                bankName = "State Bank of India",
                title = "ATM Cash Withdrawal",
                sender = "AD-SBIINB",
                body = "Your A/C ending with 1042 has been debited by Rs.2,000.00 on 30-Oct-26 by ATM WDL. Avl Bal Rs: 8,450.00 - SBI."
            ),
            SmsTemplatePreset(
                id = "sms_icici_salary",
                bankName = "ICICI Bank",
                title = "Monthly Salary Credit",
                sender = "VM-ICICIB",
                body = "Salary of INR 85,000.00 has been credited to your ICICI Bank Account XX9921 on 30-Oct-26. Info: NEFT-INFY-TECH. Available Balance INR 92,340.00."
            ),
            SmsTemplatePreset(
                id = "sms_axis_starbucks",
                bankName = "Axis Bank",
                title = "Starbucks Card Swipe",
                sender = "AX-AXISBK",
                body = "INR 630.00 spent on Axis Bank Credit Card ending 5510 at STARBUCKS COFFEE on 30-10-26 15:20:11. Avl Limit: INR 1,45,000.00."
            ),
            SmsTemplatePreset(
                id = "sms_kotak_shell",
                bankName = "Kotak Mahindra Bank",
                title = "Shell Petrol Fuel Debit",
                sender = "BZ-KOTAKB",
                body = "Rs 1,500.00 debited from Kotak Bank A/C XX8871 on 30-OCT-26 at SHELL FUEL STATION. Avl Bal: Rs 22,100.00."
            ),
            SmsTemplatePreset(
                id = "sms_paytm_grocery",
                bankName = "Paytm Payments Bank",
                title = "D-Mart Supermarket QR",
                sender = "IM-PAYTMB",
                body = "Paid Rs. 1,693.00 to DMART SUPERMARKET from Paytm Payments Bank A/C XX3312 via UPI. Ref No: 940218934. Help: paytm.com/care."
            )
        )
    }
}
