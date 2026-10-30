package com.prajwalprince94.expensetracker.data.forecast

import java.time.LocalDate
import java.util.Locale

/**
 * Health status indicators for overall and category budget burn forecasts.
 */
enum class ForecastHealthStatus(val label: String, val colorHex: Long) {
    ON_TRACK("On Track", 0xFF4ADE80),
    SAFE_SAVINGS("Surplus Ahead", 0xFF60A5FA),
    WARNING_OVERRUN("Overrun Risk", 0xFFFBBF24),
    CRITICAL_BURNOUT("Burnout Imminent", 0xFFF87171),
    EXCEEDED("Budget Exceeded", 0xFFEF4444)
}

enum class InsightSeverity {
    POSITIVE,
    INFO,
    WARNING,
    CRITICAL
}

/**
 * Contextual, actionable advice generated from the predictive spending velocity analysis.
 */
data class ActionableInsight(
    val title: String,
    val message: String,
    val categoryName: String? = null,
    val suggestedAction: String? = null,
    val severity: InsightSeverity = InsightSeverity.INFO,
    val impactBadge: String = ""
)

/**
 * Predictive forecast metrics for an individual spending category.
 */
data class CategoryForecast(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val currentSpentPaise: Long,
    val limitPaise: Long,
    val dailyVelocityPaise: Long,
    val projectedSpendPaise: Long,
    val projectedOverrunPaise: Long,
    val projectedSavingsPaise: Long,
    val daysUntilBurnout: Int?,
    val projectedBurnoutDate: LocalDate?,
    val safeDailyCapPaise: Long,
    val status: ForecastHealthStatus
) {
    val progress: Float
        get() = (currentSpentPaise.toFloat() / limitPaise.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1.5f)

    val projectedProgress: Float
        get() = (projectedSpendPaise.toFloat() / limitPaise.coerceAtLeast(1L).toFloat()).coerceIn(0f, 2.0f)

    val formattedSpent: String
        get() = "₹%,.0f".format(Locale.US, currentSpentPaise / 100.0)

    val formattedLimit: String
        get() = "₹%,.0f".format(Locale.US, limitPaise / 100.0)

    val formattedVelocity: String
        get() = "₹%,.0f/day".format(Locale.US, dailyVelocityPaise / 100.0)

    val formattedProjectedSpend: String
        get() = "₹%,.0f".format(Locale.US, projectedSpendPaise / 100.0)

    val formattedSafeDailyCap: String
        get() = "₹%,.0f/day".format(Locale.US, safeDailyCapPaise / 100.0)
}

/**
 * High-level holistic monthly budget forecast and burn rate evaluation.
 */
data class OverallBudgetForecast(
    val totalSpentPaise: Long,
    val totalLimitPaise: Long,
    val daysElapsed: Int,
    val daysRemaining: Int,
    val daysInMonth: Int,
    val currentDailyVelocityPaise: Long,
    val safeDailyAllowancePaise: Long,
    val projectedTotalSpendPaise: Long,
    val projectedVariancePaise: Long, // > 0 = Overrun, <= 0 = Savings
    val projectedBurnoutDate: LocalDate?,
    val daysUntilBurnout: Int?,
    val healthStatus: ForecastHealthStatus,
    val categoryForecasts: List<CategoryForecast>,
    val insights: List<ActionableInsight>
) {
    val isOverBudget: Boolean
        get() = totalSpentPaise > totalLimitPaise

    val utilizationPercent: Float
        get() = if (totalLimitPaise > 0L) ((totalSpentPaise.toFloat() / totalLimitPaise.toFloat()) * 100f).coerceIn(0f, 200f) else 0f

    val projectedUtilizationPercent: Float
        get() = if (totalLimitPaise > 0L) ((projectedTotalSpendPaise.toFloat() / totalLimitPaise.toFloat()) * 100f).coerceIn(0f, 300f) else 0f

    val formattedCurrentVelocity: String
        get() = "₹%,.0f/day".format(Locale.US, currentDailyVelocityPaise / 100.0)

    val formattedSafeAllowance: String
        get() = "₹%,.0f/day".format(Locale.US, safeDailyAllowancePaise / 100.0)

    val formattedProjectedSpend: String
        get() = "₹%,.0f".format(Locale.US, projectedTotalSpendPaise / 100.0)

    val formattedProjectedSavings: String
        get() = if (projectedVariancePaise <= 0) "₹%,.0f".format(Locale.US, -projectedVariancePaise / 100.0) else "₹0"

    val formattedProjectedOverrun: String
        get() = if (projectedVariancePaise > 0) "₹%,.0f".format(Locale.US, projectedVariancePaise / 100.0) else "₹0"
}
