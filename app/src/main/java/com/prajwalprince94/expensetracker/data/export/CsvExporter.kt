package com.prajwalprince94.expensetracker.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * High-performance, RFC 4180 compliant CSV Export engine for Expense Tracker.
 * Exports SQLite transactions to formatted .csv files and triggers Android Share Sheet.
 */
object CsvExporter {

    private const val CSV_HEADER = "Transaction ID,Title,Category,Type,Account,Amount (INR),Date & Time,Remark / Note"

    /**
     * Escapes a string value according to RFC 4180 CSV specifications.
     * Encloses the value in double quotes if it contains commas, double quotes, or newlines.
     */
    fun escapeCsv(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    /**
     * Generates a complete CSV content string for a list of transactions.
     */
    fun generateCsvContent(transactions: List<TransactionItem>): String {
        val sb = StringBuilder()
        sb.append(CSV_HEADER).append("\r\n")

        transactions.forEach { txn ->
            val rupees = "%.2f".format(Locale.US, txn.amountPaise / 100.0)
            val line = listOf(
                escapeCsv(txn.id),
                escapeCsv(txn.title),
                escapeCsv(txn.category),
                txn.type.name,
                escapeCsv(txn.account),
                rupees,
                escapeCsv(txn.formattedDate),
                escapeCsv(txn.note)
            ).joinToString(",")
            sb.append(line).append("\r\n")
        }

        return sb.toString()
    }

    /**
     * Exports transactions to a local CSV file in cache storage and triggers Android Share Intent.
     * @return Result containing the exported File on success, or an Exception on failure.
     */
    fun exportAndShare(context: Context, transactions: List<TransactionItem>): Result<File> {
        return try {
            if (transactions.isEmpty()) {
                return Result.failure(IllegalStateException("No transactions available to export."))
            }

            val exportDir = File(context.cacheDir, "exports").apply {
                if (!exists()) mkdirs()
            }

            val timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.US).format(LocalDateTime.now())
            val file = File(exportDir, "ExpenseTracker_Transactions_$timestamp.csv")

            FileWriter(file).use { writer ->
                writer.write(generateCsvContent(transactions))
            }

            val authority = "${context.packageName}.provider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Expense Tracker Statement — ${transactions.size} Transactions"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is your Expense Tracker statement containing ${transactions.size} records in CSV format."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Transactions CSV").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
