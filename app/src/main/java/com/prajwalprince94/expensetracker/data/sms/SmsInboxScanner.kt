package com.prajwalprince94.expensetracker.data.sms

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads and scans the Android device's SMS Inbox for unlogged financial transactions.
 * Privacy-first: all parsing executes entirely on-device with zero network requests.
 */
object SmsInboxScanner {

    private val SMS_INBOX_URI: Uri = Uri.parse("content://sms/inbox")

    /**
     * Checks if the app currently holds READ_SMS permission.
     */
    fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Scans up to [maxCount] most recent SMS messages from inbox,
     * filtering for bank financial transactions and checking against [existingTransactions].
     */
    suspend fun scanInbox(
        context: Context,
        existingTransactions: List<TransactionItem>,
        maxCount: Int = 100
    ): List<ParsedSmsExpense> = withContext(Dispatchers.IO) {
        if (!hasSmsPermission(context)) {
            return@withContext emptyList()
        }

        val results = mutableListOf<ParsedSmsExpense>()

        try {
            val projection = arrayOf(
                "_id",
                "address",
                "body",
                "date"
            )

            val cursor = context.contentResolver.query(
                SMS_INBOX_URI,
                projection,
                null,
                null,
                "date DESC"
            )

            cursor?.use { c ->
                val addressIdx = c.getColumnIndex("address")
                val bodyIdx = c.getColumnIndex("body")
                val dateIdx = c.getColumnIndex("date")

                var count = 0
                while (c.moveToNext() && count < maxCount) {
                    val address = if (addressIdx >= 0) c.getString(addressIdx) ?: "" else ""
                    val body = if (bodyIdx >= 0) c.getString(bodyIdx) ?: "" else ""
                    val timestamp = if (dateIdx >= 0) c.getLong(dateIdx) else System.currentTimeMillis()

                    if (SmsExpenseParser.isFinancialSms(address, body)) {
                        val parsed = SmsExpenseParser.parse(address, body, timestamp)
                        if (parsed != null && parsed.amountPaise > 0L) {
                            // Check deduplication against existing database transactions
                            val isDuplicate = existingTransactions.any { txn ->
                                txn.amountPaise == parsed.amountPaise &&
                                        (txn.title.contains(parsed.merchantOrBeneficiary, ignoreCase = true) ||
                                                parsed.merchantOrBeneficiary.contains(txn.title, ignoreCase = true) ||
                                                txn.formattedDate == parsed.formattedDate)
                            }
                            results.add(parsed.copy(isAlreadyLogged = isDuplicate))
                            count++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Gracefully handle security exception or cursor error
            e.printStackTrace()
        }

        results
    }
}
