package com.prajwalprince94.expensetracker.data.forecast

import com.prajwalprince94.expensetracker.data.model.CategoryBudgetItem
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionType
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Deterministic AI-style spending velocity and budget forecasting engine.
 * Computes run-rates, end-of-month projections, burnout dates, and actionable optimization advice.
 */
object BudgetForecastEngine {

    /**
     * Generates a comprehensive budget forecast based on current transactions and category budgets.
     */
    fun generateForecast(
        budgetItems: List<CategoryBudgetItem>,
        transactions: List<TransactionItem>,
        referenceDate: LocalDate = LocalDate.now()
    ): OverallBudgetForecast {
        val daysInMonth = referenceDate.lengthOfMonth()
        val daysElapsed = referenceDate.dayOfMonth.coerceIn(1, daysInMonth)
        val daysRemaining = (daysInMonth - daysElapsed).coerceAtLeast(1)

        val currentMonthTxns = filterCurrentMonthExpenses(transactions, referenceDate)

        // Calculate overall totals
        val totalSpentPaise = if (budgetItems.isNotEmpty()) {
            budgetItems.sumOf { it.spentPaise }
        } else {
            currentMonthTxns.sumOf { it.amountPaise }
        }

        val totalLimitPaise = if (budgetItems.isNotEmpty()) {
            budgetItems.sumOf { it.limitPaise }
        } else {
            (totalSpentPaise * 1.25).toLong().coerceAtLeast(1000000L) // Default fallback limit
        }

        val currentDailyVelocityPaise = totalSpentPaise / daysElapsed
        val projectedAdditionalSpendPaise = currentDailyVelocityPaise * daysRemaining
        val projectedTotalSpendPaise = totalSpentPaise + projectedAdditionalSpendPaise

        val remainingBudgetPaise = totalLimitPaise - totalSpentPaise
        val safeDailyAllowancePaise = if (daysRemaining > 0) {
            (remainingBudgetPaise.coerceAtLeast(0L) / daysRemaining)
        } else 0L

        val projectedVariancePaise = projectedTotalSpendPaise - totalLimitPaise

        // Calculate overall burnout
        val (daysUntilBurnout, projectedBurnoutDate, overallStatus) = calculateBurnout(
            currentSpent = totalSpentPaise,
            limit = totalLimitPaise,
            dailyVelocity = currentDailyVelocityPaise,
            daysRemaining = daysRemaining,
            referenceDate = referenceDate
        )

        // Calculate Per-Category Forecasts
        val categoryForecasts = budgetItems.map { budget ->
            val catSpent = budget.spentPaise
            val catLimit = budget.limitPaise
            val catVelocity = catSpent / daysElapsed
            val catProjectedAdditional = catVelocity * daysRemaining
            val catProjectedSpend = catSpent + catProjectedAdditional
            val catRemaining = catLimit - catSpent
            val catSafeCap = if (daysRemaining > 0) (catRemaining.coerceAtLeast(0L) / daysRemaining) else 0L

            val (catDaysUntilBurnout, catBurnoutDate, catStatus) = calculateBurnout(
                currentSpent = catSpent,
                limit = catLimit,
                dailyVelocity = catVelocity,
                daysRemaining = daysRemaining,
                referenceDate = referenceDate
            )

            val catOverrun = (catProjectedSpend - catLimit).coerceAtLeast(0L)
            val catSavings = (catLimit - catProjectedSpend).coerceAtLeast(0L)

            CategoryForecast(
                categoryId = budget.id,
                categoryName = budget.category,
                categoryIcon = budget.iconName,
                currentSpentPaise = catSpent,
                limitPaise = catLimit,
                dailyVelocityPaise = catVelocity,
                projectedSpendPaise = catProjectedSpend,
                projectedOverrunPaise = catOverrun,
                projectedSavingsPaise = catSavings,
                daysUntilBurnout = catDaysUntilBurnout,
                projectedBurnoutDate = catBurnoutDate,
                safeDailyCapPaise = catSafeCap,
                status = catStatus
            )
        }

        // Generate Contextual Actionable Insights
        val insights = generateInsights(
            totalSpentPaise = totalSpentPaise,
            totalLimitPaise = totalLimitPaise,
            dailyVelocityPaise = currentDailyVelocityPaise,
            safeDailyAllowancePaise = safeDailyAllowancePaise,
            projectedSpendPaise = projectedTotalSpendPaise,
            daysRemaining = daysRemaining,
            burnoutDate = projectedBurnoutDate,
            daysUntilBurnout = daysUntilBurnout,
            overallStatus = overallStatus,
            categoryForecasts = categoryForecasts,
            currentMonthTxns = currentMonthTxns
        )

        return OverallBudgetForecast(
            totalSpentPaise = totalSpentPaise,
            totalLimitPaise = totalLimitPaise,
            daysElapsed = daysElapsed,
            daysRemaining = daysRemaining,
            daysInMonth = daysInMonth,
            currentDailyVelocityPaise = currentDailyVelocityPaise,
            safeDailyAllowancePaise = safeDailyAllowancePaise,
            projectedTotalSpendPaise = projectedTotalSpendPaise,
            projectedVariancePaise = projectedVariancePaise,
            projectedBurnoutDate = projectedBurnoutDate,
            daysUntilBurnout = daysUntilBurnout,
            healthStatus = overallStatus,
            categoryForecasts = categoryForecasts,
            insights = insights
        )
    }

    private fun calculateBurnout(
        currentSpent: Long,
        limit: Long,
        dailyVelocity: Long,
        daysRemaining: Int,
        referenceDate: LocalDate
    ): Triple<Int?, LocalDate?, ForecastHealthStatus> {
        if (currentSpent >= limit) {
            return Triple(0, referenceDate, ForecastHealthStatus.EXCEEDED)
        }

        val remainingBudget = limit - currentSpent
        if (dailyVelocity <= 0L) {
            return Triple(null, null, ForecastHealthStatus.SAFE_SAVINGS)
        }

        val daysUntilBurnout = (remainingBudget.toDouble() / dailyVelocity.toDouble()).toInt()
        val projectedBurnoutDate = referenceDate.plusDays(daysUntilBurnout.toLong())

        val status = when {
            daysUntilBurnout < daysRemaining -> {
                if (daysUntilBurnout <= 4) ForecastHealthStatus.CRITICAL_BURNOUT
                else ForecastHealthStatus.WARNING_OVERRUN
            }
            dailyVelocity * daysRemaining > (remainingBudget * 0.95) -> {
                ForecastHealthStatus.WARNING_OVERRUN
            }
            (currentSpent + dailyVelocity * daysRemaining) <= (limit * 0.85) -> {
                ForecastHealthStatus.SAFE_SAVINGS
            }
            else -> {
                ForecastHealthStatus.ON_TRACK
            }
        }

        return Triple(daysUntilBurnout, projectedBurnoutDate, status)
    }

    private fun generateInsights(
        totalSpentPaise: Long,
        totalLimitPaise: Long,
        dailyVelocityPaise: Long,
        safeDailyAllowancePaise: Long,
        projectedSpendPaise: Long,
        daysRemaining: Int,
        burnoutDate: LocalDate?,
        daysUntilBurnout: Int?,
        overallStatus: ForecastHealthStatus,
        categoryForecasts: List<CategoryForecast>,
        currentMonthTxns: List<TransactionItem>
    ): List<ActionableInsight> {
        val list = mutableListOf<ActionableInsight>()
        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.US)

        // 1. Overall Burnout / Pacing Insight
        when (overallStatus) {
            ForecastHealthStatus.EXCEEDED -> {
                list.add(
                    ActionableInsight(
                        title = "Monthly Budget Exceeded",
                        message = "Total spending (₹%,.0f) has passed your total monthly limit of ₹%,.0f.".format(
                            totalSpentPaise / 100.0,
                            totalLimitPaise / 100.0
                        ),
                        suggestedAction = "Freeze discretionary spending or increase category limits in Settings.",
                        severity = InsightSeverity.CRITICAL,
                        impactBadge = "Limit Exceeded"
                    )
                )
            }
            ForecastHealthStatus.CRITICAL_BURNOUT -> {
                val formattedBurnout = burnoutDate?.format(dateFormatter) ?: "in $daysUntilBurnout days"
                list.add(
                    ActionableInsight(
                        title = "Early Burnout Alert",
                        message = "At current burn rate (₹%,.0f/day), your overall budget will exhaust on %s, %d days before month end.".format(
                            dailyVelocityPaise / 100.0,
                            formattedBurnout,
                            daysRemaining - (daysUntilBurnout ?: 0)
                        ),
                        suggestedAction = "Reduce daily spending to ₹%,.0f/day to safely complete the month.".format(
                            safeDailyAllowancePaise / 100.0
                        ),
                        severity = InsightSeverity.CRITICAL,
                        impactBadge = "Exhausts in $daysUntilBurnout days"
                    )
                )
            }
            ForecastHealthStatus.WARNING_OVERRUN -> {
                val overrunAmount = (projectedSpendPaise - totalLimitPaise) / 100.0
                list.add(
                    ActionableInsight(
                        title = "Projected Budget Overrun",
                        message = "You are currently on track to exceed your monthly limit by ₹%,.0f.".format(overrunAmount),
                        suggestedAction = "Cap daily spending to ₹%,.0f/day for the next %d days to stay within target.".format(
                            safeDailyAllowancePaise / 100.0,
                            daysRemaining
                        ),
                        severity = InsightSeverity.WARNING,
                        impactBadge = "+₹%,.0f Projected Overrun".format(overrunAmount)
                    )
                )
            }
            ForecastHealthStatus.SAFE_SAVINGS -> {
                val savingsAmount = (totalLimitPaise - projectedSpendPaise) / 100.0
                list.add(
                    ActionableInsight(
                        title = "Strong Surplus Pacing",
                        message = "You are spending conservatively and are projected to save ₹%,.0f beneath your monthly limit.".format(savingsAmount),
                        suggestedAction = "Maintain your current discipline to channel this surplus into savings or debt repayment.",
                        severity = InsightSeverity.POSITIVE,
                        impactBadge = "₹%,.0f Surplus Ahead".format(savingsAmount)
                    )
                )
            }
            ForecastHealthStatus.ON_TRACK -> {
                list.add(
                    ActionableInsight(
                        title = "Well-Calibrated Run Rate",
                        message = "Your current spending of ₹%,.0f/day aligns cleanly with your safe daily cap of ₹%,.0f/day.".format(
                            dailyVelocityPaise / 100.0,
                            safeDailyAllowancePaise / 100.0
                        ),
                        suggestedAction = "Keep daily purchases below ₹%,.0f/day to finish the month on target.".format(
                            safeDailyAllowancePaise / 100.0
                        ),
                        severity = InsightSeverity.POSITIVE,
                        impactBadge = "On Track"
                    )
                )
            }
        }

        // 2. High Velocity / Overrun Category Specific Recommendations
        val highRiskCategories = categoryForecasts
            .filter { it.status == ForecastHealthStatus.CRITICAL_BURNOUT || it.status == ForecastHealthStatus.WARNING_OVERRUN || it.status == ForecastHealthStatus.EXCEEDED }
            .sortedByDescending { it.projectedOverrunPaise }

        for (cat in highRiskCategories.take(2)) {
            if (cat.status == ForecastHealthStatus.EXCEEDED) {
                list.add(
                    ActionableInsight(
                        title = "${cat.categoryName} Limit Reached",
                        message = "You have spent ₹%,.0f of your ₹%,.0f budget for ${cat.categoryName}.".format(
                            cat.currentSpentPaise / 100.0,
                            cat.limitPaise / 100.0
                        ),
                        categoryName = cat.categoryName,
                        suggestedAction = "Avoid additional ${cat.categoryName} expenses until the next billing cycle.",
                        severity = InsightSeverity.CRITICAL,
                        impactBadge = "100% Utilized"
                    )
                )
            } else {
                val trimPerDay = (cat.dailyVelocityPaise - cat.safeDailyCapPaise).coerceAtLeast(0L) / 100.0
                list.add(
                    ActionableInsight(
                        title = "Trim ${cat.categoryName} Velocity",
                        message = "${cat.categoryName} is burning at ₹%,.0f/day (projected +₹%,.0f overrun).".format(
                            cat.dailyVelocityPaise / 100.0,
                            cat.projectedOverrunPaise / 100.0
                        ),
                        categoryName = cat.categoryName,
                        suggestedAction = "Trim by ₹%,.0f/day to cap spend at ₹%,.0f/day for the remaining %d days.".format(
                            trimPerDay,
                            cat.safeDailyCapPaise / 100.0,
                            daysRemaining
                        ),
                        severity = InsightSeverity.WARNING,
                        impactBadge = "+₹%,.0f Overrun".format(cat.projectedOverrunPaise / 100.0)
                    )
                )
            }
        }

        // 3. Spending Spike / Surge Detection (Recent 3 days vs overall)
        if (currentMonthTxns.size >= 5) {
            val recentTxns = currentMonthTxns.filter {
                it.formattedDate.contains("Today", ignoreCase = true) ||
                it.formattedDate.contains("Yesterday", ignoreCase = true)
            }
            val recentPaise = recentTxns.sumOf { it.amountPaise }
            if (totalSpentPaise > 0 && recentPaise > 0) {
                val recentRatio = recentPaise.toFloat() / totalSpentPaise.toFloat()
                if (recentRatio >= 0.40f) {
                    list.add(
                        ActionableInsight(
                            title = "Recent Spending Spike Detected",
                            message = "%.0f%% of this month's total expenses (₹%,.0f) occurred over the last 48 hours.".format(
                                recentRatio * 100f,
                                recentPaise / 100.0
                            ),
                            suggestedAction = "Expect temporary velocity elevation; pace your upcoming discretionary expenses.",
                            severity = InsightSeverity.INFO,
                            impactBadge = "%.0f%% in 48h".format(recentRatio * 100f)
                        )
                    )
                }
            }
        }

        return list
    }

    private fun filterCurrentMonthExpenses(
        transactions: List<TransactionItem>,
        referenceDate: LocalDate
    ): List<TransactionItem> {
        val currentMonthName = referenceDate.format(DateTimeFormatter.ofPattern("MMM", Locale.US))
        val currentMonthYear = referenceDate.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.US))

        val matched = transactions.filter { txn ->
            txn.type == TransactionType.EXPENSE && (
                txn.formattedDate.contains("Today", ignoreCase = true) ||
                txn.formattedDate.contains("Yesterday", ignoreCase = true) ||
                txn.formattedDate.contains(currentMonthYear, ignoreCase = true) ||
                txn.formattedDate.contains(currentMonthName, ignoreCase = true)
            )
        }

        return if (matched.isNotEmpty()) matched else transactions.filter { it.type == TransactionType.EXPENSE }
    }
}
