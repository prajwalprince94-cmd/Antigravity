package com.prajwalprince94.expensetracker.data.currency

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.util.Locale

/**
 * Information and metadata for a supported world currency.
 */
data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val name: String,
    val flagEmoji: String,
    val defaultRateToInr: Double, // 1 Unit of this currency = X INR
    val decimalDigits: Int = 2
)

/**
 * High-performance, offline-first multi-currency and FX conversion engine.
 * Supports on-device cached exchange rates, cross-currency conversions,
 * formatted output, and manual/live rate refresh.
 */
object CurrencyEngine {

    val SUPPORTED_CURRENCIES: List<CurrencyInfo> = listOf(
        CurrencyInfo(code = "INR", symbol = "₹", name = "Indian Rupee", flagEmoji = "🇮🇳", defaultRateToInr = 1.0),
        CurrencyInfo(code = "USD", symbol = "$", name = "US Dollar", flagEmoji = "🇺🇸", defaultRateToInr = 86.80),
        CurrencyInfo(code = "EUR", symbol = "€", name = "Euro", flagEmoji = "🇪🇺", defaultRateToInr = 94.20),
        CurrencyInfo(code = "GBP", symbol = "£", name = "British Pound", flagEmoji = "🇬🇧", defaultRateToInr = 112.50),
        CurrencyInfo(code = "AED", symbol = "د.إ", name = "UAE Dirham", flagEmoji = "🇦🇪", defaultRateToInr = 23.65),
        CurrencyInfo(code = "SGD", symbol = "S$", name = "Singapore Dollar", flagEmoji = "🇸🇬", defaultRateToInr = 65.20),
        CurrencyInfo(code = "JPY", symbol = "¥", name = "Japanese Yen", flagEmoji = "🇯🇵", defaultRateToInr = 0.58, decimalDigits = 0),
        CurrencyInfo(code = "CAD", symbol = "C$", name = "Canadian Dollar", flagEmoji = "🇨🇦", defaultRateToInr = 62.40),
        CurrencyInfo(code = "AUD", symbol = "A$", name = "Australian Dollar", flagEmoji = "🇦🇺", defaultRateToInr = 56.50),
        CurrencyInfo(code = "THB", symbol = "฿", name = "Thai Baht", flagEmoji = "🇹🇭", defaultRateToInr = 2.45),
        CurrencyInfo(code = "SAR", symbol = "﷼", name = "Saudi Riyal", flagEmoji = "🇸🇦", defaultRateToInr = 23.15)
    )

    private val currencyMap = SUPPORTED_CURRENCIES.associateBy { it.code }

    // Live/cached exchange rates map: CurrencyCode -> Rate to INR
    private val _exchangeRates = MutableStateFlow<Map<String, Double>>(
        SUPPORTED_CURRENCIES.associate { it.code to it.defaultRateToInr }
    )
    val exchangeRates: StateFlow<Map<String, Double>> = _exchangeRates.asStateFlow()

    private val _lastUpdatedMillis = MutableStateFlow(System.currentTimeMillis())
    val lastUpdatedMillis: StateFlow<Long> = _lastUpdatedMillis.asStateFlow()

    /**
     * Returns metadata for given currency code or default INR fallback.
     */
    fun getCurrency(code: String?): CurrencyInfo {
        if (code == null) return currencyMap["INR"] ?: SUPPORTED_CURRENCIES.first()
        return currencyMap[code.uppercase(Locale.US)] ?: CurrencyInfo(
            code = code.uppercase(Locale.US),
            symbol = code.uppercase(Locale.US),
            name = code,
            flagEmoji = "🌐",
            defaultRateToInr = 1.0
        )
    }

    /**
     * Gets current exchange rate from one currency to another (default base: INR).
     */
    fun getExchangeRate(fromCode: String, toCode: String = "INR"): Double {
        val fromUpper = fromCode.uppercase(Locale.US)
        val toUpper = toCode.uppercase(Locale.US)
        if (fromUpper == toUpper) return 1.0

        val currentRates = _exchangeRates.value
        val fromRateToInr = currentRates[fromUpper] ?: getCurrency(fromUpper).defaultRateToInr
        val toRateToInr = currentRates[toUpper] ?: getCurrency(toUpper).defaultRateToInr

        if (toRateToInr == 0.0) return 1.0
        return fromRateToInr / toRateToInr
    }

    /**
     * Converts a monetary amount in units (e.g., 50.0 USD) to target currency units.
     */
    fun convert(amountUnits: Double, fromCode: String, toCode: String = "INR"): Double {
        if (amountUnits == 0.0) return 0.0
        val rate = getExchangeRate(fromCode, toCode)
        return amountUnits * rate
    }

    /**
     * Converts foreign currency units directly into base INR Paise (Long) for financial correctness.
     */
    fun convertToInrPaise(amountUnits: Double, fromCode: String): Long {
        if (fromCode.equals("INR", ignoreCase = true)) {
            return (amountUnits * 100).toLong()
        }
        val inrUnits = convert(amountUnits, fromCode, "INR")
        return (inrUnits * 100).toLong()
    }

    /**
     * Formats an amount with currency symbol and precision.
     */
    fun formatAmount(amountUnits: Double, currencyCode: String): String {
        val curr = getCurrency(currencyCode)
        val format = if (curr.decimalDigits == 0) {
            "%,.0f"
        } else {
            "%,.2f"
        }
        return "${curr.symbol}${format.format(Locale.US, amountUnits)}"
    }

    /**
     * Formats INR paise into specified foreign currency string.
     */
    fun formatInrPaiseToCurrency(inrPaise: Long, targetCurrencyCode: String): String {
        val inrUnits = inrPaise / 100.0
        val targetUnits = convert(inrUnits, "INR", targetCurrencyCode)
        return formatAmount(targetUnits, targetCurrencyCode)
    }

    /**
     * Refreshes exchange rates (simulated deterministic live refresh for stability with micro-variations).
     */
    fun refreshRates() {
        val current = _exchangeRates.value.toMutableMap()
        SUPPORTED_CURRENCIES.forEach { curr ->
            if (curr.code != "INR") {
                // Micro variance of +- 0.2% around default rate to simulate real-time market float
                val variance = (Math.sin(System.currentTimeMillis() / 60000.0 + curr.code.hashCode()) * 0.002)
                val newRate = curr.defaultRateToInr * (1.0 + variance)
                current[curr.code] = Math.round(newRate * 100.0) / 100.0
            }
        }
        _exchangeRates.value = current
        _lastUpdatedMillis.value = System.currentTimeMillis()
    }
}
