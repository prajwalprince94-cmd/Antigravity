package com.prajwalprince94.expensetracker.data.sms

import com.prajwalprince94.expensetracker.ui.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

/**
 * High-accuracy deterministic regex and dictionary engine for financial SMS parsing.
 * Analyzes debit/credit SMS from Indian banks and UPI providers to extract transaction details.
 */
object SmsExpenseParser {

    private val BANK_PREFIX_MAP = mapOf(
        "HDFCBK" to "HDFC Bank",
        "HDFC" to "HDFC Bank",
        "SBIINB" to "State Bank of India",
        "SBIPAY" to "State Bank of India",
        "SBI" to "State Bank of India",
        "ICICIB" to "ICICI Bank",
        "ICICI" to "ICICI Bank",
        "AXISBK" to "Axis Bank",
        "AXIS" to "Axis Bank",
        "KOTAKB" to "Kotak Mahindra Bank",
        "KOTAK" to "Kotak Mahindra Bank",
        "PAYTMB" to "Paytm Payments Bank",
        "PAYTM" to "Paytm Payments Bank",
        "INDUSB" to "IndusInd Bank",
        "YESBNK" to "Yes Bank",
        "PNBSMS" to "Punjab National Bank",
        "BARODA" to "Bank of Baroda",
        "UNIONB" to "Union Bank",
        "CANBNK" to "Canara Bank",
        "IDFCFB" to "IDFC First Bank",
        "RBLBNK" to "RBL Bank"
    )

    private val KNOWN_MERCHANTS = mapOf(
        "swiggy" to ("Swiggy" to "Food & Dining"),
        "zomato" to ("Zomato" to "Food & Dining"),
        "starbucks" to ("Starbucks Coffee" to "Food & Dining"),
        "mcdonald" to ("McDonald's" to "Food & Dining"),
        "kfc" to ("KFC" to "Food & Dining"),
        "subway" to ("Subway" to "Food & Dining"),
        "burger king" to ("Burger King" to "Food & Dining"),
        "pizza hut" to ("Pizza Hut" to "Food & Dining"),
        "domino" to ("Domino's Pizza" to "Food & Dining"),
        "chai point" to ("Chai Point" to "Food & Dining"),
        "dmart" to ("D-Mart Supermarket" to "Groceries"),
        "blinkit" to ("Blinkit Groceries" to "Groceries"),
        "zepto" to ("Zepto Groceries" to "Groceries"),
        "bigbasket" to ("BigBasket" to "Groceries"),
        "nature's basket" to ("Nature's Basket" to "Groceries"),
        "shell" to ("Shell Petrol Station" to "Transportation"),
        "petrol" to ("Petrol Pump" to "Transportation"),
        "fuel" to ("Fuel Station" to "Transportation"),
        "hpcl" to ("HPCL Petrol Pump" to "Transportation"),
        "bpcl" to ("BPCL Petrol Pump" to "Transportation"),
        "uber" to ("Uber Ride" to "Transportation"),
        "ola" to ("Ola Ride" to "Transportation"),
        "irctc" to ("IRCTC Railways" to "Transportation"),
        "metro" to ("Metro Transit" to "Transportation"),
        "amazon" to ("Amazon India" to "Shopping"),
        "flipkart" to ("Flipkart" to "Shopping"),
        "myntra" to ("Myntra Fashion" to "Shopping"),
        "zara" to ("Zara Fashion" to "Shopping"),
        "h&m" to ("H&M" to "Shopping"),
        "croma" to ("Croma Electronics" to "Shopping"),
        "reliance digital" to ("Reliance Digital" to "Shopping"),
        "apollo" to ("Apollo Pharmacy" to "Healthcare"),
        "medplus" to ("MedPlus Pharmacy" to "Healthcare"),
        "pharmacy" to ("Local Pharmacy" to "Healthcare"),
        "hospital" to ("Hospital Care" to "Healthcare"),
        "netflix" to ("Netflix Subscription" to "Entertainment"),
        "spotify" to ("Spotify" to "Entertainment"),
        "bookmyshow" to ("BookMyShow" to "Entertainment"),
        "prime" to ("Amazon Prime" to "Entertainment"),
        "pvr" to ("PVR Cinemas" to "Entertainment"),
        "airtel" to ("Airtel Telecom" to "Rent & Bills"),
        "jio" to ("Jio Telecom" to "Rent & Bills"),
        "bescom" to ("Electricity Bill" to "Rent & Bills"),
        "electricity" to ("Electricity Bill" to "Rent & Bills"),
        "atm wdl" to ("ATM Cash Withdrawal" to "Personal"),
        "atm" to ("ATM Withdrawal" to "Personal"),
        "salary" to ("Monthly Salary" to "Salary")
    )

    /**
     * Checks if a raw SMS matches typical financial transactional patterns.
     */
    fun isFinancialSms(sender: String, body: String): Boolean {
        val lowerBody = body.lowercase(Locale.ROOT)
        val lowerSender = sender.lowercase(Locale.ROOT)

        val hasAmount = lowerBody.contains("rs") || lowerBody.contains("inr") || lowerBody.contains("₹")
        val hasFinancialKeywords = lowerBody.contains("debited") ||
                lowerBody.contains("credited") ||
                lowerBody.contains("spent") ||
                lowerBody.contains("paid") ||
                lowerBody.contains("withdrawn") ||
                lowerBody.contains("received") ||
                lowerBody.contains("transferred") ||
                lowerBody.contains("refund")

        // Exclude OTP, promo, and authentication messages
        val isOtpOrPromo = lowerBody.contains("otp") ||
                lowerBody.contains("one time password") ||
                lowerBody.contains("verification code") ||
                lowerBody.contains("login to") ||
                lowerBody.contains("pre-approved") ||
                lowerBody.contains("congratulations! you are eligible")

        val isSenderBank = BANK_PREFIX_MAP.keys.any { lowerSender.contains(it.lowercase(Locale.ROOT)) } ||
                lowerSender.matches(Regex("^[a-zA-Z]{2}-[a-zA-Z0-9]+$"))

        return hasAmount && hasFinancialKeywords && !isOtpOrPromo && isSenderBank
    }

    /**
     * Parses a raw SMS into a structured ParsedSmsExpense object.
     * Returns null if no valid financial transaction can be extracted.
     */
    fun parse(sender: String, body: String, timestampMillis: Long = System.currentTimeMillis()): ParsedSmsExpense? {
        val cleanBody = body.replace("\n", " ").trim()
        val lowerBody = cleanBody.lowercase(Locale.ROOT)

        // 1. Determine Bank Name
        val bankName = extractBankName(sender, cleanBody)

        // 2. Determine Transaction Type
        val type = extractTransactionType(lowerBody)

        // 3. Extract Amount in Paise
        val amountPaise = extractAmountPaise(cleanBody)
        if (amountPaise <= 0L) {
            return null // No valid transaction amount found
        }

        // 4. Extract Account Last 4
        val accountLast4 = extractAccountLast4(cleanBody)

        // 5. Extract Merchant or Beneficiary
        val (merchant, category) = extractMerchantAndCategory(cleanBody, type)

        // 6. Extract Reference / UTR Number
        val refNumber = extractReferenceNumber(cleanBody)

        // 7. Extract Date or format timestamp
        val dateString = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(timestampMillis))

        return ParsedSmsExpense(
            sender = sender,
            bankName = bankName,
            amountPaise = amountPaise,
            type = type,
            accountLast4 = accountLast4,
            merchantOrBeneficiary = merchant,
            dateMillis = timestampMillis,
            formattedDate = dateString,
            rawBody = cleanBody,
            suggestedCategory = category,
            suggestedAccount = bankName,
            refNumber = refNumber
        )
    }

    private fun extractBankName(sender: String, body: String): String {
        val upperSender = sender.uppercase(Locale.ROOT)
        for ((key, name) in BANK_PREFIX_MAP) {
            if (upperSender.contains(key)) {
                return name
            }
        }
        val upperBody = body.uppercase(Locale.ROOT)
        for ((key, name) in BANK_PREFIX_MAP) {
            if (upperBody.contains(key)) {
                return name
            }
        }
        return "Bank Account"
    }

    private fun extractTransactionType(lowerBody: String): TransactionType {
        val debitIndex = lowerBody.indexOfFirstOrNull(listOf("debited", "spent", "paid", "withdrawn", "charged", "sent", "atm wdl"))
        val creditIndex = lowerBody.indexOfFirstOrNull(listOf("credited", "received", "refund", "salary", "deposited"))

        return when {
            debitIndex != null && creditIndex != null -> {
                // If both occur, priority to earlier action on user's account
                if (debitIndex < creditIndex) TransactionType.EXPENSE else TransactionType.INCOME
            }
            creditIndex != null -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }
    }

    private fun String.indexOfFirstOrNull(keywords: List<String>): Int? {
        val indices = keywords.map { this.indexOf(it) }.filter { it >= 0 }
        return indices.minOrNull()
    }

    private fun extractAmountPaise(body: String): Long {
        // Exclude balance statements from amount extraction
        // First remove "Avl Bal", "Available Balance", "Bal Rs", etc. to avoid extracting balance as spent amount
        val bodyWithoutBalance = body.replace(
            Regex("(?i)(?:avl\\.?\\s*bal(?:ance)?|available\\s*balance|bal(?:ance)?\\s*:?)\\s*(?:rs\\.?|inr|₹)?\\s*[:\\s]*[\\d,]+(?:\\.\\d{1,2})?"),
            ""
        )

        // Match Rs. / INR / ₹ followed by numbers
        val pattern = Pattern.compile("(?i)(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)")
        val matcher = pattern.matcher(bodyWithoutBalance)

        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "")
            val parsedDouble = amountStr?.toDoubleOrNull()
            if (parsedDouble != null && parsedDouble > 0.0) {
                return (parsedDouble * 100).toLong()
            }
        }

        // Secondary fallback: numbers followed by INR / Rs
        val fallbackPattern = Pattern.compile("([\\d,]+(?:\\.\\d{1,2})?)\\s*(?:inr|rs\\.?)", Pattern.CASE_INSENSITIVE)
        val fallbackMatcher = fallbackPattern.matcher(bodyWithoutBalance)
        if (fallbackMatcher.find()) {
            val amountStr = fallbackMatcher.group(1)?.replace(",", "")
            val parsedDouble = amountStr?.toDoubleOrNull()
            if (parsedDouble != null && parsedDouble > 0.0) {
                return (parsedDouble * 100).toLong()
            }
        }

        return 0L
    }

    private fun extractAccountLast4(body: String): String? {
        val regex = Regex("(?i)(?:a\\/c|acct|account|card)\\s*(?:no\\.?)?\\s*(?:ending\\s*(?:with)?)?\\s*[*xX]*(\\d{3,4})")
        val match = regex.find(body)
        return match?.groupValues?.get(1)
    }

    private fun extractMerchantAndCategory(body: String, type: TransactionType): Pair<String, String> {
        val lower = body.lowercase(Locale.ROOT)

        // 1. Check known brand dictionary
        for ((key, pair) in KNOWN_MERCHANTS) {
            if (lower.contains(key)) {
                return pair.first to pair.second
            }
        }

        // 2. Check for UPI transfer target (e.g., "to Swiggy UPI" or "at STARBUCKS")
        val merchantPattern = Regex("(?i)(?:to|at|towards|vpa|info:?)\\s+([A-Za-z0-9\\.\\-_@& ]{2,25})")
        val match = merchantPattern.find(body)
        if (match != null) {
            val candidate = match.groupValues[1].trim()
                .replace(Regex("(?i)(upi|ref|avl|bal|call|on|dated|via).*"), "")
                .trim()
            if (candidate.length in 2..25) {
                val cat = if (type == TransactionType.INCOME) "Income" else "Food & Dining"
                return candidate to cat
            }
        }

        // Fallback default
        return if (type == TransactionType.INCOME) {
            "Income Deposit" to "Income"
        } else {
            "Bank Transaction" to "Food & Dining"
        }
    }

    private fun extractReferenceNumber(body: String): String? {
        val refPattern = Regex("(?i)(?:ref(?:\\s*no)?|upi\\s*ref|rrn|utr)\\s*[:\\s]*([A-Za-z0-9]{8,16})")
        return refPattern.find(body)?.groupValues?.get(1)
    }
}
