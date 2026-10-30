package com.prajwalprince94.expensetracker.data.receipt

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

/**
 * Intelligent deterministic OCR text parser for physical receipts and digital bills.
 * Accurately extracts merchant, total amount in paise, transaction date, line items,
 * and auto-suggests category and payment account.
 */
object ReceiptTextParser {

    private val KNOWN_MERCHANTS = mapOf(
        "starbucks" to ("Starbucks Coffee" to "Food & Dining"),
        "dmart" to ("D-Mart Supermarket" to "Groceries"),
        "d-mart" to ("D-Mart Supermarket" to "Groceries"),
        "mcdonald" to ("McDonald's" to "Food & Dining"),
        "kfc" to ("KFC" to "Food & Dining"),
        "subway" to ("Subway" to "Food & Dining"),
        "burger king" to ("Burger King" to "Food & Dining"),
        "pizza hut" to ("Pizza Hut" to "Food & Dining"),
        "domino" to ("Domino's Pizza" to "Food & Dining"),
        "shell" to ("Shell Fuel Station" to "Transportation"),
        "hpcl" to ("HPCL Petrol Pump" to "Transportation"),
        "bpcl" to ("BPCL Petrol Pump" to "Transportation"),
        "indian oil" to ("Indian Oil Fuel" to "Transportation"),
        "apollo" to ("Apollo Pharmacy" to "Healthcare"),
        "medplus" to ("MedPlus Pharmacy" to "Healthcare"),
        "pharmacy" to ("Local Pharmacy" to "Healthcare"),
        "reliance digital" to ("Reliance Digital" to "Shopping"),
        "croma" to ("Croma Electronics" to "Shopping"),
        "swiggy" to ("Swiggy Delivery" to "Food & Dining"),
        "zomato" to ("Zomato Delivery" to "Food & Dining"),
        "uber" to ("Uber Ride" to "Transportation"),
        "ola" to ("Ola Ride" to "Transportation"),
        "amazon" to ("Amazon Shopping" to "Shopping"),
        "flipkart" to ("Flipkart" to "Shopping"),
        "decathlon" to ("Decathlon Sports" to "Shopping"),
        "zara" to ("Zara Fashion" to "Shopping"),
        "h&m" to ("H&M Fashion" to "Shopping"),
        "airtel" to ("Airtel Telecom" to "Rent & Bills"),
        "jio" to ("Jio Telecom" to "Rent & Bills")
    )

    fun parse(rawText: String): ParsedReceipt {
        if (rawText.isBlank()) {
            return ParsedReceipt(
                merchantName = "Scanned Receipt",
                totalAmountPaise = 0L,
                rawText = ""
            )
        }

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val merchant = extractMerchant(lines)
        val (amountPaise, amountConfidence) = extractTotalAmount(lines, rawText)
        val dateString = extractDate(lines) ?: SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
        val category = determineCategory(rawText, merchant)
        val account = determineAccount(rawText)
        val lineItems = extractLineItems(lines)

        return ParsedReceipt(
            merchantName = merchant,
            totalAmountPaise = amountPaise,
            formattedDate = dateString,
            suggestedCategory = category,
            suggestedAccount = account,
            lineItems = lineItems,
            rawText = rawText,
            confidence = amountConfidence
        )
    }

    private fun extractMerchant(lines: List<String>): String {
        val fullTextLower = lines.joinToString(" ").lowercase(Locale.ROOT)
        for ((key, pair) in KNOWN_MERCHANTS) {
            if (fullTextLower.contains(key)) {
                return pair.first
            }
        }

        // Fallback: Pick top clean heading line
        for (line in lines.take(5)) {
            val clean = line.replace(Regex("[^A-Za-z0-9 &.'-]"), "").trim()
            val lower = clean.lowercase(Locale.ROOT)
            if (clean.length in 3..35 &&
                !lower.contains("tax invoice") &&
                !lower.contains("cash memo") &&
                !lower.contains("bill no") &&
                !lower.contains("receipt") &&
                !lower.contains("welcome") &&
                !lower.contains("date:")
            ) {
                return clean
            }
        }

        return lines.firstOrNull()?.take(25) ?: "Scanned Store"
    }

    private fun extractTotalAmount(lines: List<String>, fullText: String): Pair<Long, Float> {
        val totalKeywords = listOf(
            "grand total", "total payable", "net payable", "net amount", "total bill",
            "invoice total", "total amount", "final total", "balance due", "amount payable",
            "total", "subtotal", "sub total"
        )

        // 1. Check lines containing specific total keywords
        for (kw in totalKeywords) {
            for (line in lines.reversed()) {
                val lower = line.lowercase(Locale.ROOT)
                if (lower.contains(kw)) {
                    val parsed = parseAmountFromLine(line)
                    if (parsed != null && parsed > 0L) {
                        return parsed to 0.98f
                    }
                }
            }
        }

        // 2. Scan lines with currency symbols (₹, Rs, INR)
        val currencyRegex = Pattern.compile("(?:₹|rs\\.?|inr)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE)
        val matcher = currencyRegex.matcher(fullText)
        var maxCurrencyAmount = 0L

        while (matcher.find()) {
            val amtStr = matcher.group(1)?.replace(",", "") ?: continue
            val paise = (amtStr.toDoubleOrNull()?.times(100))?.toLong() ?: continue
            if (paise in 100L..50000000L && paise > maxCurrencyAmount) {
                maxCurrencyAmount = paise
            }
        }

        if (maxCurrencyAmount > 0L) {
            return maxCurrencyAmount to 0.85f
        }

        // 3. Fallback: find highest isolated decimal number on later lines
        val decimalRegex = Pattern.compile("\\b([0-9]{1,5}\\.[0-9]{2})\\b")
        var highestPaise = 0L
        for (line in lines.reversed()) {
            val dMatcher = decimalRegex.matcher(line)
            while (dMatcher.find()) {
                val amt = (dMatcher.group(1)?.toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                if (amt in 100L..50000000L && amt > highestPaise) {
                    highestPaise = amt
                }
            }
        }

        return if (highestPaise > 0L) highestPaise to 0.70f else 0L to 0.30f
    }

    private fun parseAmountFromLine(line: String): Long? {
        val regex = Pattern.compile("([0-9,]+(?:\\.[0-9]{1,2})?)")
        val matcher = regex.matcher(line)
        var lastValid: Long? = null

        while (matcher.find()) {
            val candidate = matcher.group(1)?.replace(",", "") ?: continue
            val paise = (candidate.toDoubleOrNull()?.times(100))?.toLong()
            if (paise != null && paise in 100L..50000000L) {
                lastValid = paise
            }
        }
        return lastValid
    }

    private fun extractDate(lines: List<String>): String? {
        // Regex patterns for: 28/10/2026, 28-Oct-2026, 2026-10-28, 28.10.26
        val datePatterns = listOf(
            Pattern.compile("\\b(0?[1-9]|[12][0-9]|3[01])[-/.](0?[1-9]|1[012])[-/.](20\\d\\d|\\d\\d)\\b"),
            Pattern.compile("\\b(0?[1-9]|[12][0-9]|3[01])[-/.](Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[-/.](20\\d\\d|\\d\\d)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(20\\d\\d)[-/.](0?[1-9]|1[012])[-/.](0?[1-9]|[12][0-9]|3[01])\\b")
        )

        for (line in lines) {
            for (p in datePatterns) {
                val m = p.matcher(line)
                if (m.find()) {
                    val rawDate = m.group(0) ?: continue
                    val normalized = tryParseDate(rawDate)
                    if (normalized != null) return normalized
                }
            }
        }
        return null
    }

    private fun tryParseDate(input: String): String? {
        val clean = input.replace('.', '/').replace('-', '/')
        val formats = listOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.US),
            SimpleDateFormat("dd/MMM/yyyy", Locale.US),
            SimpleDateFormat("yyyy/MM/dd", Locale.US),
            SimpleDateFormat("dd/MM/yy", Locale.US)
        )
        val targetFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

        for (sdf in formats) {
            try {
                val d = sdf.parse(clean)
                if (d != null) {
                    return targetFormat.format(d)
                }
            } catch (_: Exception) {}
        }
        return null
    }

    private fun determineCategory(fullText: String, merchant: String): String {
        val lower = (fullText + " " + merchant).lowercase(Locale.ROOT)

        for ((key, pair) in KNOWN_MERCHANTS) {
            if (lower.contains(key)) return pair.second
        }

        return when {
            lower.contains("coffee") || lower.contains("cafe") || lower.contains("cookie") ||
            lower.contains("burger") || lower.contains("pizza") || lower.contains("tea") ||
            lower.contains("restaurant") || lower.contains("dinner") || lower.contains("lunch") -> "Food & Dining"

            lower.contains("supermarket") || lower.contains("grocery") || lower.contains("milk") ||
            lower.contains("dal") || lower.contains("rice") || lower.contains("vegetable") ||
            lower.contains("oil") || lower.contains("flour") || lower.contains("sugar") -> "Groceries"

            lower.contains("fuel") || lower.contains("petrol") || lower.contains("diesel") ||
            lower.contains("pump") || lower.contains("uber") || lower.contains("ola") ||
            lower.contains("cab") || lower.contains("transport") -> "Transportation"

            lower.contains("pharmacy") || lower.contains("medical") || lower.contains("medicine") ||
            lower.contains("capsule") || lower.contains("tablet") || lower.contains("hospital") ||
            lower.contains("bandage") || lower.contains("dolo") -> "Healthcare"

            lower.contains("digital") || lower.contains("charger") || lower.contains("cable") ||
            lower.contains("gadget") || lower.contains("clothes") || lower.contains("shoes") ||
            lower.contains("fashion") || lower.contains("shopping") -> "Shopping"

            lower.contains("wifi") || lower.contains("broadband") || lower.contains("bill") ||
            lower.contains("electricity") || lower.contains("recharge") -> "Rent & Bills"

            else -> "Food & Dining"
        }
    }

    private fun determineAccount(fullText: String): String {
        val lower = fullText.lowercase(Locale.ROOT)
        return when {
            lower.contains("credit card") || lower.contains("visa") || lower.contains("mastercard") -> "ICICI Credit Card"
            lower.contains("hdfc") -> "HDFC Bank"
            lower.contains("sbi") -> "SBI Savings"
            lower.contains("cash") -> "Cash Wallet"
            else -> "Paytm / UPI"
        }
    }

    private fun extractLineItems(lines: List<String>): List<ReceiptLineItem> {
        val items = mutableListOf<ReceiptLineItem>()
        val itemPattern = Pattern.compile("^[0-9.]*\\s*([A-Za-z0-9 &.'-]{3,30})\\s+[:=-]?\\s*(?:₹|rs\\.?|inr)?\\s*([0-9,]+(?:\\.[0-9]{2}))", Pattern.CASE_INSENSITIVE)

        for (line in lines) {
            val lower = line.lowercase(Locale.ROOT)
            if (lower.contains("total") || lower.contains("subtotal") || lower.contains("cgst") ||
                lower.contains("sgst") || lower.contains("tax") || lower.contains("round off")
            ) {
                continue
            }

            val m = itemPattern.matcher(line)
            if (m.find()) {
                val desc = m.group(1)?.trim() ?: continue
                val amtStr = m.group(2)?.replace(",", "") ?: continue
                val paise = (amtStr.toDoubleOrNull()?.times(100))?.toLong() ?: continue
                if (paise in 500L..5000000L) {
                    items.add(ReceiptLineItem(name = desc, amountPaise = paise))
                }
            }
        }
        return items.take(8)
    }
}
