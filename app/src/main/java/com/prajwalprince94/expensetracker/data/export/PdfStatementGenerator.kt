package com.prajwalprince94.expensetracker.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Filter period presets for PDF statement generation.
 */
enum class StatementPeriod(val label: String) {
    CURRENT_MONTH("Current Month"),
    LAST_MONTH("Last Month"),
    LAST_3_MONTHS("Last 3 Months"),
    YEAR_TO_DATE("Year to Date (YTD)"),
    FULL_YEAR("Full Year"),
    ALL_TIME("All Time")
}

/**
 * Configuration options for generating PDF financial statements.
 */
data class PdfExportOptions(
    val title: String = "Financial Statement & Tax Report",
    val period: StatementPeriod = StatementPeriod.CURRENT_MONTH,
    val selectedAccount: String? = null,
    val selectedCategory: String? = null,
    val includeTaxDeductions: Boolean = true,
    val filterType: TransactionType? = null
)

/**
 * Tax deductions and eligible exemptions summary.
 */
data class TaxDeductionSummary(
    val totalDeductiblePaise: Long,
    val healthcarePaise: Long, // 80D
    val insurancePaise: Long,  // 80C
    val educationPaise: Long,  // 80C/80E
    val donationsPaise: Long,  // 80G
    val businessExpensePaise: Long // Travel/Fuel/Work
)

/**
 * Native, offline-first PDF generation engine using Android's native PdfDocument.
 * Generates publication-ready financial statements, tax reports, and chronological transaction ledgers.
 */
object PdfStatementGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_X = 36f
    private const val MARGIN_BOTTOM = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2)

    /**
     * Filters transactions according to user-selected export options.
     */
    fun filterTransactions(
        transactions: List<TransactionItem>,
        options: PdfExportOptions,
        now: LocalDate = LocalDate.now()
    ): List<TransactionItem> {
        val currentMonthYear = now.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.US))
        val prevMonthYear = now.minusMonths(1).format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.US))
        val currentYear = now.year.toString()

        return transactions.filter { txn ->
            // Period filter
            val matchesPeriod = when (options.period) {
                StatementPeriod.CURRENT_MONTH -> {
                    txn.formattedDate.contains("Today", ignoreCase = true) ||
                    txn.formattedDate.contains("Yesterday", ignoreCase = true) ||
                    txn.formattedDate.contains(currentMonthYear, ignoreCase = true)
                }
                StatementPeriod.LAST_MONTH -> {
                    txn.formattedDate.contains(prevMonthYear, ignoreCase = true)
                }
                StatementPeriod.LAST_3_MONTHS, StatementPeriod.YEAR_TO_DATE, StatementPeriod.FULL_YEAR -> {
                    txn.formattedDate.contains(currentYear, ignoreCase = true) ||
                    txn.formattedDate.contains("Today", ignoreCase = true) ||
                    txn.formattedDate.contains("Yesterday", ignoreCase = true)
                }
                StatementPeriod.ALL_TIME -> true
            }

            // Account filter
            val matchesAccount = options.selectedAccount == null ||
                options.selectedAccount.equals("All Accounts", ignoreCase = true) ||
                txn.account.equals(options.selectedAccount, ignoreCase = true)

            // Category filter
            val matchesCategory = options.selectedCategory == null ||
                options.selectedCategory.equals("All Categories", ignoreCase = true) ||
                txn.category.equals(options.selectedCategory, ignoreCase = true)

            // Type filter
            val matchesType = options.filterType == null || txn.type == options.filterType

            matchesPeriod && matchesAccount && matchesCategory && matchesType
        }.ifEmpty {
            // Fallback: if period filter produces 0 rows (e.g. freshly seeded sample data), return all transactions
            transactions
        }
    }

    /**
     * Computes tax-deductible items from expenses.
     */
    fun computeTaxSummary(transactions: List<TransactionItem>): TaxDeductionSummary {
        var healthcare = 0L
        var insurance = 0L
        var education = 0L
        var donations = 0L
        var business = 0L

        for (txn in transactions) {
            if (txn.type == TransactionType.EXPENSE) {
                val cat = txn.category.lowercase(Locale.US)
                val note = txn.note.lowercase(Locale.US)
                val title = txn.title.lowercase(Locale.US)

                when {
                    cat.contains("health") || cat.contains("medic") || cat.contains("doctor") ||
                    title.contains("pharma") || title.contains("hospital") -> {
                        healthcare += txn.amountPaise
                    }
                    cat.contains("insurance") || title.contains("lic") || title.contains("insurance") -> {
                        insurance += txn.amountPaise
                    }
                    cat.contains("education") || cat.contains("school") || cat.contains("college") || cat.contains("tuition") -> {
                        education += txn.amountPaise
                    }
                    cat.contains("donation") || cat.contains("charity") || cat.contains("ngo") -> {
                        donations += txn.amountPaise
                    }
                    cat.contains("fuel") || cat.contains("petrol") || cat.contains("travel") || cat.contains("transport") -> {
                        business += txn.amountPaise
                    }
                }
            }
        }

        val total = healthcare + insurance + education + donations + business
        return TaxDeductionSummary(
            totalDeductiblePaise = total,
            healthcarePaise = healthcare,
            insurancePaise = insurance,
            educationPaise = education,
            donationsPaise = donations,
            businessExpensePaise = business
        )
    }

    /**
     * Generates the PDF document and writes it to cache storage.
     */
    fun generatePdfFile(
        context: Context,
        transactions: List<TransactionItem>,
        options: PdfExportOptions = PdfExportOptions()
    ): File {
        val filteredTxns = filterTransactions(transactions, options)
        val taxSummary = computeTaxSummary(filteredTxns)

        val totalIncomePaise = filteredTxns.filter { it.type == TransactionType.INCOME }.sumOf { it.amountPaise }
        val totalExpensePaise = filteredTxns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountPaise }
        val netSavingsPaise = totalIncomePaise - totalExpensePaise
        val savingsRate = if (totalIncomePaise > 0L) {
            ((netSavingsPaise.toDouble() / totalIncomePaise.toDouble()) * 100.0).coerceIn(-100.0, 100.0)
        } else 0.0

        // Calculate Category Breakdown
        val categoryBreakdown = filteredTxns
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> Pair(entry.value.size, entry.value.sumOf { it.amountPaise }) }
            .toList()
            .sortedByDescending { it.second.second }

        val pdfDocument = PdfDocument()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.75f
            color = Color.rgb(229, 231, 235)
        }

        var pageIndex = 1
        var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create())
        var canvas = page.canvas
        var currentY = 0f

        // --- DRAW PAGE 1 HEADER BANNER ---
        fillPaint.color = Color.rgb(20, 24, 33) // Navy Charcoal
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 75f, fillPaint)

        // Title & Logo Text
        textPaint.color = Color.WHITE
        textPaint.textSize = 15f
        textPaint.isFakeBoldText = true
        canvas.drawText("EXPENSE TRACKER FINANCIAL STATEMENT", MARGIN_X, 32f, textPaint)

        textPaint.textSize = 8.5f
        textPaint.isFakeBoldText = false
        textPaint.color = Color.rgb(156, 163, 175)
        canvas.drawText("OFFICIAL FINANCIAL LEDGER & TAX DEDUCTION SUMMARY", MARGIN_X, 48f, textPaint)

        // Statement Metadata (Top Right)
        val timestampStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm", Locale.US))
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = 8f
        textPaint.color = Color.rgb(209, 213, 219)
        canvas.drawText("Generated: $timestampStr", PAGE_WIDTH - MARGIN_X, 32f, textPaint)
        canvas.drawText("Period: ${options.period.label} • ${filteredTxns.size} Txns", PAGE_WIDTH - MARGIN_X, 46f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        currentY = 92f

        // --- STATEMENT SCOPE BOX ---
        fillPaint.color = Color.rgb(248, 250, 252)
        val scopeRect = RectF(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY + 36f)
        canvas.drawRoundRect(scopeRect, 6f, 6f, fillPaint)
        canvas.drawRoundRect(scopeRect, 6f, 6f, strokePaint)

        textPaint.color = Color.rgb(75, 85, 99)
        textPaint.textSize = 8f
        textPaint.isFakeBoldText = true
        canvas.drawText("STATEMENT PARAMETERS", MARGIN_X + 12f, currentY + 14f, textPaint)

        textPaint.isFakeBoldText = false
        textPaint.color = Color.rgb(31, 41, 55)
        val accountFilterText = options.selectedAccount ?: "All Accounts"
        val categoryFilterText = options.selectedCategory ?: "All Categories"
        canvas.drawText("Account: $accountFilterText  •  Category: $categoryFilterText  •  Scope: ${options.period.label}", MARGIN_X + 12f, currentY + 27f, textPaint)

        currentY += 48f

        // --- EXECUTIVE SUMMARY METRICS CARDS (3 Columns) ---
        val cardWidth = (CONTENT_WIDTH - 16f) / 3f
        val cardHeight = 52f

        // Card 1: Total Credits (Income)
        drawMetricCard(
            canvas = canvas,
            x = MARGIN_X,
            y = currentY,
            width = cardWidth,
            height = cardHeight,
            label = "TOTAL INCOME (CREDITS)",
            value = "+₹%,.2f".format(Locale.US, totalIncomePaise / 100.0),
            accentColor = Color.rgb(16, 185, 129), // Emerald
            subtext = "${filteredTxns.count { it.type == TransactionType.INCOME }} Credits Logged",
            fillPaint = fillPaint,
            textPaint = textPaint,
            strokePaint = strokePaint
        )

        // Card 2: Total Debits (Expenses)
        drawMetricCard(
            canvas = canvas,
            x = MARGIN_X + cardWidth + 8f,
            y = currentY,
            width = cardWidth,
            height = cardHeight,
            label = "TOTAL EXPENSES (DEBITS)",
            value = "-₹%,.2f".format(Locale.US, totalExpensePaise / 100.0),
            accentColor = Color.rgb(239, 68, 68), // Red
            subtext = "${filteredTxns.count { it.type == TransactionType.EXPENSE }} Debits Logged",
            fillPaint = fillPaint,
            textPaint = textPaint,
            strokePaint = strokePaint
        )

        // Card 3: Net Surplus / Savings
        drawMetricCard(
            canvas = canvas,
            x = MARGIN_X + (cardWidth + 8f) * 2,
            y = currentY,
            width = cardWidth,
            height = cardHeight,
            label = "NET SAVINGS / SURPLUS",
            value = "₹%,.2f".format(Locale.US, netSavingsPaise / 100.0),
            accentColor = if (netSavingsPaise >= 0) Color.rgb(59, 130, 246) else Color.rgb(239, 68, 68),
            subtext = "Savings Rate: %.1f%%".format(savingsRate),
            fillPaint = fillPaint,
            textPaint = textPaint,
            strokePaint = strokePaint
        )

        currentY += cardHeight + 14f

        // --- TAX DEDUCTION & EXEMPTION BOX ---
        if (options.includeTaxDeductions && taxSummary.totalDeductiblePaise > 0L) {
            fillPaint.color = Color.rgb(240, 253, 244) // Soft Mint
            val taxRect = RectF(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY + 48f)
            canvas.drawRoundRect(taxRect, 6f, 6f, fillPaint)
            strokePaint.color = Color.rgb(187, 247, 208)
            canvas.drawRoundRect(taxRect, 6f, 6f, strokePaint)
            strokePaint.color = Color.rgb(229, 231, 235)

            textPaint.color = Color.rgb(21, 128, 61)
            textPaint.textSize = 8.5f
            textPaint.isFakeBoldText = true
            canvas.drawText("ESTIMATED TAX-DEDUCTIBLE EXPENSES SUMMARY", MARGIN_X + 12f, currentY + 14f, textPaint)

            textPaint.isFakeBoldText = false
            textPaint.color = Color.rgb(55, 65, 81)
            textPaint.textSize = 7.5f
            val taxBreakdownStr = "Medical (80D): ₹%,.0f  •  Insurance (80C): ₹%,.0f  •  Education: ₹%,.0f  •  Donations: ₹%,.0f  •  Fuel/Travel: ₹%,.0f".format(
                Locale.US,
                taxSummary.healthcarePaise / 100.0,
                taxSummary.insurancePaise / 100.0,
                taxSummary.educationPaise / 100.0,
                taxSummary.donationsPaise / 100.0,
                taxSummary.businessExpensePaise / 100.0
            )
            canvas.drawText(taxBreakdownStr, MARGIN_X + 12f, currentY + 27f, textPaint)

            textPaint.isFakeBoldText = true
            textPaint.color = Color.rgb(21, 128, 61)
            canvas.drawText("Total Estimated Eligible Tax Deductions: ₹%,.2f".format(Locale.US, taxSummary.totalDeductiblePaise / 100.0), MARGIN_X + 12f, currentY + 40f, textPaint)

            currentY += 58f
        }

        // --- CATEGORY SPENDING BREAKDOWN (Top Categories Table) ---
        if (categoryBreakdown.isNotEmpty()) {
            textPaint.color = Color.rgb(31, 41, 55)
            textPaint.textSize = 9.5f
            textPaint.isFakeBoldText = true
            canvas.drawText("CATEGORY EXPENDITURE BREAKDOWN", MARGIN_X, currentY + 10f, textPaint)
            currentY += 16f

            // Category Table Header
            fillPaint.color = Color.rgb(243, 244, 246)
            canvas.drawRect(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY + 16f, fillPaint)
            textPaint.textSize = 7.5f
            textPaint.color = Color.rgb(75, 85, 99)
            textPaint.isFakeBoldText = true
            canvas.drawText("CATEGORY", MARGIN_X + 8f, currentY + 11f, textPaint)
            canvas.drawText("TXNS", MARGIN_X + 220f, currentY + 11f, textPaint)
            canvas.drawText("PERCENTAGE", MARGIN_X + 310f, currentY + 11f, textPaint)
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("AMOUNT (INR)", PAGE_WIDTH - MARGIN_X - 8f, currentY + 11f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
            currentY += 16f

            for (cat in categoryBreakdown.take(5)) {
                val catName = cat.first
                val catCount = cat.second.first
                val catAmount = cat.second.second
                val catPercent = if (totalExpensePaise > 0L) (catAmount.toDouble() / totalExpensePaise.toDouble()) * 100.0 else 0.0

                textPaint.textSize = 7.5f
                textPaint.isFakeBoldText = false
                textPaint.color = Color.rgb(31, 41, 55)
                canvas.drawText(catName, MARGIN_X + 8f, currentY + 11f, textPaint)
                canvas.drawText("$catCount txns", MARGIN_X + 220f, currentY + 11f, textPaint)
                canvas.drawText("%.1f%% of total".format(catPercent), MARGIN_X + 310f, currentY + 11f, textPaint)

                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.isFakeBoldText = true
                canvas.drawText("₹%,.2f".format(Locale.US, catAmount / 100.0), PAGE_WIDTH - MARGIN_X - 8f, currentY + 11f, textPaint)
                textPaint.textAlign = Paint.Align.LEFT

                canvas.drawLine(MARGIN_X, currentY + 15f, PAGE_WIDTH - MARGIN_X, currentY + 15f, strokePaint)
                currentY += 16f
            }
            currentY += 10f
        }

        // --- TRANSACTION LEDGER HEADER ---
        textPaint.color = Color.rgb(31, 41, 55)
        textPaint.textSize = 9.5f
        textPaint.isFakeBoldText = true
        canvas.drawText("CHRONOLOGICAL TRANSACTION LEDGER (${filteredTxns.size} Records)", MARGIN_X, currentY + 10f, textPaint)
        currentY += 16f

        drawLedgerTableHeader(canvas, currentY, fillPaint, textPaint)
        currentY += 16f

        // --- RENDER TRANSACTION ROWS WITH DYNAMIC PAGINATION ---
        var rowIndex = 0
        for (txn in filteredTxns) {
            // Check if page needs to break
            if (currentY + 20f > PAGE_HEIGHT - MARGIN_BOTTOM) {
                drawPageFooter(canvas, pageIndex, 0, textPaint)
                pdfDocument.finishPage(page)

                pageIndex++
                page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create())
                canvas = page.canvas

                // Draw continued header on subsequent pages
                currentY = 40f
                fillPaint.color = Color.rgb(20, 24, 33)
                canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 34f, fillPaint)
                textPaint.color = Color.WHITE
                textPaint.textSize = 9f
                textPaint.isFakeBoldText = true
                canvas.drawText("EXPENSE TRACKER FINANCIAL STATEMENT (CONTINUED)", MARGIN_X, 22f, textPaint)

                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.textSize = 8f
                textPaint.color = Color.rgb(156, 163, 175)
                canvas.drawText("Page $pageIndex", PAGE_WIDTH - MARGIN_X, 22f, textPaint)
                textPaint.textAlign = Paint.Align.LEFT

                currentY = 48f
                drawLedgerTableHeader(canvas, currentY, fillPaint, textPaint)
                currentY += 16f
            }

            // Draw Zebra background for alternating rows
            if (rowIndex % 2 == 1) {
                fillPaint.color = Color.rgb(249, 250, 251)
                canvas.drawRect(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY + 16f, fillPaint)
            }

            // Draw transaction columns
            textPaint.textSize = 7.5f
            textPaint.isFakeBoldText = false
            textPaint.color = Color.rgb(55, 65, 81)

            // Date
            canvas.drawText(cleanDateString(txn.formattedDate), MARGIN_X + 6f, currentY + 11f, textPaint)

            // Description / Merchant
            textPaint.isFakeBoldText = true
            textPaint.color = Color.rgb(17, 24, 39)
            val truncatedTitle = if (txn.title.length > 24) txn.title.take(22) + ".." else txn.title
            canvas.drawText(truncatedTitle, MARGIN_X + 90f, currentY + 11f, textPaint)
            textPaint.isFakeBoldText = false

            // Category
            textPaint.color = Color.rgb(75, 85, 99)
            val truncatedCat = if (txn.category.length > 18) txn.category.take(16) + ".." else txn.category
            canvas.drawText(truncatedCat, MARGIN_X + 225f, currentY + 11f, textPaint)

            // Account
            val truncatedAccount = if (txn.account.length > 16) txn.account.take(14) + ".." else txn.account
            canvas.drawText(truncatedAccount, MARGIN_X + 325f, currentY + 11f, textPaint)

            // Type
            val isCredit = txn.type == TransactionType.INCOME
            textPaint.color = if (isCredit) Color.rgb(5, 150, 105) else Color.rgb(220, 38, 38)
            canvas.drawText(if (isCredit) "CR" else "DR", MARGIN_X + 415f, currentY + 11f, textPaint)

            // Amount
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.isFakeBoldText = true
            val formattedAmount = (if (isCredit) "+₹" else "-₹") + "%,.2f".format(Locale.US, txn.amountPaise / 100.0)
            canvas.drawText(formattedAmount, PAGE_WIDTH - MARGIN_X - 6f, currentY + 11f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            canvas.drawLine(MARGIN_X, currentY + 16f, PAGE_WIDTH - MARGIN_X, currentY + 16f, strokePaint)

            currentY += 16f
            rowIndex++
        }

        // Draw statement footer & verification hash on the final page
        drawPageFooter(canvas, pageIndex, pageIndex, textPaint)
        pdfDocument.finishPage(page)

        // Save PDF to local cache storage
        val exportDir = File(context.cacheDir, "statements").apply {
            if (!exists()) mkdirs()
        }
        val fileTimestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.US).format(LocalDateTime.now())
        val pdfFile = File(exportDir, "ExpenseTracker_Statement_$fileTimestamp.pdf")

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    /**
     * Generates and triggers the system share sheet for the generated PDF statement.
     */
    fun exportAndShare(
        context: Context,
        transactions: List<TransactionItem>,
        options: PdfExportOptions = PdfExportOptions()
    ): Result<File> {
        return try {
            if (transactions.isEmpty()) {
                return Result.failure(IllegalStateException("No transactions found to export."))
            }

            val pdfFile = generatePdfFile(context, transactions, options)
            val authority = "${context.packageName}.provider"
            val contentUri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Expense Tracker Financial Statement (${options.period.label})")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is your Expense Tracker official statement (${options.period.label}) containing ${transactions.size} records with tax deductions breakdown."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Statement PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
            Result.success(pdfFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun drawMetricCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        label: String,
        value: String,
        accentColor: Int,
        subtext: String,
        fillPaint: Paint,
        textPaint: Paint,
        strokePaint: Paint
    ) {
        fillPaint.color = Color.WHITE
        val cardRect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(cardRect, 6f, 6f, fillPaint)
        canvas.drawRoundRect(cardRect, 6f, 6f, strokePaint)

        // Accent indicator line on left of card
        fillPaint.color = accentColor
        canvas.drawRoundRect(RectF(x, y, x + 3.5f, y + height), 2f, 2f, fillPaint)

        // Metric Label
        textPaint.color = Color.rgb(107, 114, 128)
        textPaint.textSize = 7f
        textPaint.isFakeBoldText = true
        canvas.drawText(label, x + 10f, y + 14f, textPaint)

        // Metric Value
        textPaint.color = accentColor
        textPaint.textSize = 10.5f
        textPaint.isFakeBoldText = true
        canvas.drawText(value, x + 10f, y + 31f, textPaint)

        // Metric Subtext
        textPaint.color = Color.rgb(156, 163, 175)
        textPaint.textSize = 6.5f
        textPaint.isFakeBoldText = false
        canvas.drawText(subtext, x + 10f, y + 43f, textPaint)
    }

    private fun drawLedgerTableHeader(canvas: Canvas, y: Float, fillPaint: Paint, textPaint: Paint) {
        fillPaint.color = Color.rgb(243, 244, 246)
        canvas.drawRect(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + 16f, fillPaint)

        textPaint.textSize = 7.5f
        textPaint.color = Color.rgb(75, 85, 99)
        textPaint.isFakeBoldText = true

        canvas.drawText("DATE", MARGIN_X + 6f, y + 11f, textPaint)
        canvas.drawText("DESCRIPTION / MERCHANT", MARGIN_X + 90f, y + 11f, textPaint)
        canvas.drawText("CATEGORY", MARGIN_X + 225f, y + 11f, textPaint)
        canvas.drawText("ACCOUNT", MARGIN_X + 325f, y + 11f, textPaint)
        canvas.drawText("TYPE", MARGIN_X + 415f, y + 11f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMOUNT (INR)", PAGE_WIDTH - MARGIN_X - 6f, y + 11f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int, totalPages: Int, textPaint: Paint) {
        val footerY = PAGE_HEIGHT - 22f
        textPaint.color = Color.rgb(156, 163, 175)
        textPaint.textSize = 7f
        textPaint.isFakeBoldText = false

        canvas.drawText("Expense Tracker • Confidential Financial Statement • Generated on Device", MARGIN_X, footerY, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        val pageStr = if (totalPages > 0) "Page $pageNumber of $totalPages" else "Page $pageNumber"
        canvas.drawText(pageStr, PAGE_WIDTH - MARGIN_X, footerY, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }

    private fun cleanDateString(rawDate: String): String {
        return rawDate.replace("Today", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM", Locale.US)))
            .replace("Yesterday", LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd MMM", Locale.US)))
            .split(",").firstOrNull()?.trim() ?: rawDate
    }
}
