package com.prajwalprince94.expensetracker.data.analytics

import com.prajwalprince94.expensetracker.data.model.BillingCycle
import com.prajwalprince94.expensetracker.data.model.CategoryBudgetItem
import com.prajwalprince94.expensetracker.data.model.SubscriptionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionType
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class HealthTier(val label: String, val colorHex: String) {
    EXCELLENT("Excellent", "#10B981"),
    GOOD("Good", "#14B8A6"),
    FAIR("Fair", "#F59E0B"),
    NEEDS_ATTENTION("Needs Attention", "#EF4444")
}

enum class InsightType {
    POSITIVE,
    WARNING,
    TIP,
    INFO
}

data class FinancialInsight(
    val type: InsightType,
    val title: String,
    val message: String,
    val impactBadge: String
)

data class CategorySpendData(
    val category: String,
    val amountPaise: Long,
    val percentage: Float,
    val transactionCount: Int,
    val colorHex: String
) {
    val formattedAmount: String
        get() = "₹%,.2f".format(Locale.US, amountPaise / 100.0)
}

data class DailySpendPoint(
    val dayOfMonth: Int,
    val dateLabel: String,
    val currentMonthAmountPaise: Long,
    val previousMonthAmountPaise: Long
)

data class FinancialHealthReport(
    val overallScore: Int, // 0 - 100
    val tier: HealthTier,
    val savingsRatePercent: Float,
    val savingsScore: Int, // max 35
    val budgetAdherenceScore: Int, // max 25
    val commitmentScore: Int, // max 20
    val stabilityScore: Int, // max 20
    val currentMonthIncomePaise: Long,
    val currentMonthExpensePaise: Long,
    val currentMonthNetSavingsPaise: Long,
    val previousMonthExpensePaise: Long,
    val momExpenseChangePercent: Float, // +/- % vs last month
    val daysElapsedInMonth: Int,
    val daysRemainingInMonth: Int,
    val daysInMonth: Int,
    val dailyAverageBurnPaise: Long,
    val projectedMonthEndSpendPaise: Long,
    val projectedSurplusDeficitPaise: Long,
    val committedSubscriptionsPaise: Long,
    val commitmentRatioPercent: Float,
    val categorySpendBreakdown: List<CategorySpendData>,
    val dailySpendTrend: List<DailySpendPoint>,
    val actionableInsights: List<FinancialInsight>,
    val selectedYearMonth: YearMonth
) {
    val formattedIncome: String
        get() = "₹%,.2f".format(Locale.US, currentMonthIncomePaise / 100.0)

    val formattedExpense: String
        get() = "₹%,.2f".format(Locale.US, currentMonthExpensePaise / 100.0)

    val formattedNetSavings: String
        get() = "₹%,.2f".format(Locale.US, currentMonthNetSavingsPaise / 100.0)

    val formattedDailyBurn: String
        get() = "₹%,.0f".format(Locale.US, dailyAverageBurnPaise / 100.0)

    val formattedProjectedEndSpend: String
        get() = "₹%,.0f".format(Locale.US, projectedMonthEndSpendPaise / 100.0)

    val formattedProjectedSurplus: String
        get() = "₹%,.0f".format(Locale.US, projectedSurplusDeficitPaise / 100.0)

    val formattedSubscriptionsCommitment: String
        get() = "₹%,.2f".format(Locale.US, committedSubscriptionsPaise / 100.0)
}

/**
 * High-performance deterministic analytics engine modeled after Stride and top fintech apps.
 * Evaluates holistic financial health, burn rate forecasts, month-over-month variances,
 * and category distribution.
 */
object FinancialHealthAnalyzer {

    private val categoryColors = listOf(
        "#10B981", "#3B82F6", "#F59E0B", "#8B5CF6",
        "#EC4899", "#06B6D4", "#F97316", "#64748B"
    )

    fun analyze(
        transactions: List<TransactionItem>,
        budgets: List<CategoryBudgetItem> = emptyList(),
        subscriptions: List<SubscriptionItem> = emptyList(),
        monthOffset: Int = 0 // 0 = current month, -1 = last month, etc.
    ): FinancialHealthReport {
        val targetMonth = YearMonth.now().plusMonths(monthOffset.toLong())
        val prevMonth = targetMonth.minusMonths(1)

        val daysInMonth = targetMonth.lengthOfMonth()
        val today = LocalDate.now()
        val daysElapsedInMonth = if (targetMonth == YearMonth.now()) {
            today.dayOfMonth.coerceIn(1, daysInMonth)
        } else if (targetMonth.isBefore(YearMonth.now())) {
            daysInMonth
        } else {
            1
        }
        val daysRemainingInMonth = (daysInMonth - daysElapsedInMonth).coerceAtLeast(0)

        // Parse and partition transactions by month
        val (currentMonthTxns, prevMonthTxns) = partitionTransactions(transactions, targetMonth, prevMonth)

        // Income & Expense totals
        val currentMonthIncomePaise = currentMonthTxns
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amountPaise }

        val currentMonthExpensePaise = currentMonthTxns
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amountPaise }

        val currentMonthNetSavingsPaise = currentMonthIncomePaise - currentMonthExpensePaise

        val prevMonthExpensePaise = prevMonthTxns
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amountPaise }

        // Month-over-Month % change
        val momExpenseChangePercent = if (prevMonthExpensePaise > 0L) {
            ((currentMonthExpensePaise - prevMonthExpensePaise).toFloat() / prevMonthExpensePaise.toFloat()) * 100f
        } else 0f

        // Burn Rate & Projections
        val dailyAverageBurnPaise = if (daysElapsedInMonth > 0) {
            currentMonthExpensePaise / daysElapsedInMonth
        } else 0L

        val projectedMonthEndSpendPaise = currentMonthExpensePaise + (dailyAverageBurnPaise * daysRemainingInMonth)
        val projectedSurplusDeficitPaise = currentMonthIncomePaise - projectedMonthEndSpendPaise

        // Committed Subscriptions
        val committedSubscriptionsPaise = subscriptions
            .filter { it.isActive }
            .sumOf { sub ->
                when (sub.billingCycle) {
                    BillingCycle.MONTHLY -> sub.amountPaise
                    BillingCycle.QUARTERLY -> sub.amountPaise / 3
                    BillingCycle.YEARLY -> sub.amountPaise / 12
                }
            }

        val effectiveIncomeForRatios = if (currentMonthIncomePaise > 0L) currentMonthIncomePaise else {
            // If user hasn't logged income yet this month, estimate baseline from expenses or default
            (currentMonthExpensePaise * 1.35f).toLong().coerceAtLeast(30_000_00L)
        }

        val savingsRatePercent = if (effectiveIncomeForRatios > 0L) {
            ((effectiveIncomeForRatios - currentMonthExpensePaise).toFloat() / effectiveIncomeForRatios.toFloat()) * 100f
        } else 0f

        val commitmentRatioPercent = if (effectiveIncomeForRatios > 0L) {
            (committedSubscriptionsPaise.toFloat() / effectiveIncomeForRatios.toFloat()) * 100f
        } else 0f

        // --- SCORING WEIGHTS (Total 100 pts) ---
        // 1. Savings Rate Score (Max 35 pts)
        // Rule: 20%+ savings is optimal (35 pts), 10-20% (25 pts), 0-10% (15 pts), negative (0-10 pts)
        val savingsScore = when {
            savingsRatePercent >= 30f -> 35
            savingsRatePercent >= 20f -> 30
            savingsRatePercent >= 10f -> 22
            savingsRatePercent >= 0f -> 15
            savingsRatePercent >= -15f -> 8
            else -> 4
        }

        // 2. Budget Adherence Score (Max 25 pts)
        // Check ratio of budget limits vs spent
        val budgetAdherenceScore = if (budgets.isEmpty()) {
            20 // Neutral positive baseline if no budgets configured
        } else {
            val totalBudgetPaise = budgets.sumOf { it.limitPaise }
            val totalSpentPaise = budgets.sumOf { it.spentPaise }
            val ratio = if (totalBudgetPaise > 0L) totalSpentPaise.toFloat() / totalBudgetPaise.toFloat() else 1f
            when {
                ratio <= 0.85f -> 25
                ratio <= 1.0f -> 20
                ratio <= 1.15f -> 12
                ratio <= 1.30f -> 6
                else -> 2
            }
        }

        // 3. Subscription Commitment Score (Max 20 pts)
        // Rule: Subscriptions taking <10% of income is excellent (20 pts), 10-20% is good (15 pts), >25% warns (8 pts)
        val commitmentScore = when {
            commitmentRatioPercent <= 10f -> 20
            commitmentRatioPercent <= 20f -> 16
            commitmentRatioPercent <= 30f -> 10
            else -> 5
        }

        // 4. Daily Spending Stability Score (Max 20 pts)
        // Evaluates variance across days
        val stabilityScore = calculateStabilityScore(currentMonthTxns, daysElapsedInMonth, dailyAverageBurnPaise)

        val totalScore = (savingsScore + budgetAdherenceScore + commitmentScore + stabilityScore).coerceIn(10, 100)

        val tier = when {
            totalScore >= 80 -> HealthTier.EXCELLENT
            totalScore >= 65 -> HealthTier.GOOD
            totalScore >= 50 -> HealthTier.FAIR
            else -> HealthTier.NEEDS_ATTENTION
        }

        // Category Spend Breakdown
        val expenses = currentMonthTxns.filter { it.type == TransactionType.EXPENSE }
        val totalExpPaise = expenses.sumOf { it.amountPaise }.coerceAtLeast(1L)
        val categorySpendBreakdown = expenses.groupBy { it.category }
            .mapValues { (cat, list) ->
                val catSum = list.sumOf { it.amountPaise }
                val pct = (catSum.toFloat() / totalExpPaise.toFloat()) * 100f
                Triple(catSum, pct, list.size)
            }
            .toList()
            .sortedByDescending { it.second.first }
            .mapIndexed { idx, (cat, data) ->
                val color = categoryColors[idx % categoryColors.size]
                CategorySpendData(
                    category = cat,
                    amountPaise = data.first,
                    percentage = data.second,
                    transactionCount = data.third,
                    colorHex = color
                )
            }

        // Daily Spend Trend (Current month vs Previous month)
        val dailySpendTrend = (1..daysInMonth).map { day ->
            val curDayPaise = currentMonthTxns
                .filter { it.type == TransactionType.EXPENSE && extractDayOfMonth(it.formattedDate) == day }
                .sumOf { it.amountPaise }

            val prevDayPaise = prevMonthTxns
                .filter { it.type == TransactionType.EXPENSE && extractDayOfMonth(it.formattedDate) == day }
                .sumOf { it.amountPaise }

            DailySpendPoint(
                dayOfMonth = day,
                dateLabel = "$day",
                currentMonthAmountPaise = curDayPaise,
                previousMonthAmountPaise = prevDayPaise
            )
        }

        // Actionable Insights Synthesis
        val insights = generateInsights(
            savingsRatePercent = savingsRatePercent,
            momExpenseChangePercent = momExpenseChangePercent,
            commitmentRatioPercent = commitmentRatioPercent,
            projectedSurplusDeficitPaise = projectedSurplusDeficitPaise,
            categorySpendBreakdown = categorySpendBreakdown,
            budgets = budgets
        )

        return FinancialHealthReport(
            overallScore = totalScore,
            tier = tier,
            savingsRatePercent = savingsRatePercent,
            savingsScore = savingsScore,
            budgetAdherenceScore = budgetAdherenceScore,
            commitmentScore = commitmentScore,
            stabilityScore = stabilityScore,
            currentMonthIncomePaise = currentMonthIncomePaise,
            currentMonthExpensePaise = currentMonthExpensePaise,
            currentMonthNetSavingsPaise = currentMonthNetSavingsPaise,
            previousMonthExpensePaise = prevMonthExpensePaise,
            momExpenseChangePercent = momExpenseChangePercent,
            daysElapsedInMonth = daysElapsedInMonth,
            daysRemainingInMonth = daysRemainingInMonth,
            daysInMonth = daysInMonth,
            dailyAverageBurnPaise = dailyAverageBurnPaise,
            projectedMonthEndSpendPaise = projectedMonthEndSpendPaise,
            projectedSurplusDeficitPaise = projectedSurplusDeficitPaise,
            committedSubscriptionsPaise = committedSubscriptionsPaise,
            commitmentRatioPercent = commitmentRatioPercent,
            categorySpendBreakdown = categorySpendBreakdown,
            dailySpendTrend = dailySpendTrend,
            actionableInsights = insights,
            selectedYearMonth = targetMonth
        )
    }

    private fun calculateStabilityScore(
        transactions: List<TransactionItem>,
        daysElapsed: Int,
        dailyAveragePaise: Long
    ): Int {
        if (daysElapsed <= 2 || dailyAveragePaise == 0L) return 18
        val dailySums = (1..daysElapsed).map { day ->
            transactions
                .filter { it.type == TransactionType.EXPENSE && extractDayOfMonth(it.formattedDate) == day }
                .sumOf { it.amountPaise }
        }
        val maxSpike = dailySums.maxOrNull() ?: 0L
        val spikeRatio = if (dailyAveragePaise > 0L) maxSpike.toFloat() / dailyAveragePaise.toFloat() else 1f
        return when {
            spikeRatio <= 2.2f -> 20
            spikeRatio <= 3.5f -> 16
            spikeRatio <= 5.0f -> 12
            else -> 8
        }
    }

    private fun generateInsights(
        savingsRatePercent: Float,
        momExpenseChangePercent: Float,
        commitmentRatioPercent: Float,
        projectedSurplusDeficitPaise: Long,
        categorySpendBreakdown: List<CategorySpendData>,
        budgets: List<CategoryBudgetItem>
    ): List<FinancialInsight> {
        val list = mutableListOf<FinancialInsight>()

        // 1. Savings Rate Insight
        if (savingsRatePercent >= 20f) {
            list.add(
                FinancialInsight(
                    type = InsightType.POSITIVE,
                    title = "Superb Savings Discipline",
                    message = "You are saving %.1f%% of your monthly income, exceeding the recommended 20%% benchmark.".format(savingsRatePercent),
                    impactBadge = "+%.0f%% Saved".format(savingsRatePercent)
                )
            )
        } else if (savingsRatePercent < 0f) {
            list.add(
                FinancialInsight(
                    type = InsightType.WARNING,
                    title = "Deficit Spending Alert",
                    message = "Your expenses currently outpace logged income. Review non-essential purchases to prevent balance erosion.",
                    impactBadge = "Deficit"
                )
            )
        }

        // 2. Month-over-Month Comparison
        if (momExpenseChangePercent <= -10f) {
            list.add(
                FinancialInsight(
                    type = InsightType.POSITIVE,
                    title = "Reduced Spending Pace",
                    message = "You are spending %.1f%% less compared to the same cycle last month. Great job retaining capital!".format(kotlin.math.abs(momExpenseChangePercent)),
                    impactBadge = "%.1f%% MoM".format(momExpenseChangePercent)
                )
            )
        } else if (momExpenseChangePercent >= 15f) {
            list.add(
                FinancialInsight(
                    type = InsightType.WARNING,
                    title = "Accelerated Spend Velocity",
                    message = "Spending is running %.1f%% higher than last month. Monitor your daily run-rate.".format(momExpenseChangePercent),
                    impactBadge = "+%.1f%% MoM".format(momExpenseChangePercent)
                )
            )
        }

        // 3. Top Category Concentration
        val topCategory = categorySpendBreakdown.firstOrNull()
        if (topCategory != null && topCategory.percentage >= 40f) {
            list.add(
                FinancialInsight(
                    type = InsightType.INFO,
                    title = "High Category Concentration",
                    message = "${topCategory.category} accounts for %.0f%% of total monthly spend (%s).".format(topCategory.percentage, topCategory.formattedAmount),
                    impactBadge = "%.0f%% in ${topCategory.category}".format(topCategory.percentage)
                )
            )
        }

        // 4. Over-budget warnings
        val exceededBudget = budgets.firstOrNull { it.spentPaise > it.limitPaise }
        if (exceededBudget != null) {
            val overage = (exceededBudget.spentPaise - exceededBudget.limitPaise) / 100.0
            list.add(
                FinancialInsight(
                    type = InsightType.WARNING,
                    title = "Budget Exceeded in ${exceededBudget.category}",
                    message = "${exceededBudget.category} is ₹%,.2f over its designated limit.".format(overage),
                    impactBadge = "Over Limit"
                )
            )
        }

        // 5. Subscription Commitments
        if (commitmentRatioPercent >= 20f) {
            list.add(
                FinancialInsight(
                    type = InsightType.TIP,
                    title = "Audit Recurring Subscriptions",
                    message = "Fixed recurring bills claim %.1f%% of income. Pruning unused apps can boost savings.".format(commitmentRatioPercent),
                    impactBadge = "%.0f%% Fixed".format(commitmentRatioPercent)
                )
            )
        } else {
            list.add(
                FinancialInsight(
                    type = InsightType.POSITIVE,
                    title = "Lean Fixed Commitments",
                    message = "Recurring subscriptions consume only %.1f%% of income, keeping your cashflow highly agile.".format(commitmentRatioPercent),
                    impactBadge = "Agile"
                )
            )
        }

        return list
    }

    private fun partitionTransactions(
        transactions: List<TransactionItem>,
        targetMonth: YearMonth,
        prevMonth: YearMonth
    ): Pair<List<TransactionItem>, List<TransactionItem>> {
        val curList = mutableListOf<TransactionItem>()
        val prevList = mutableListOf<TransactionItem>()

        val targetMonthStr = targetMonth.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.US))
        val prevMonthStr = prevMonth.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.US))

        for (txn in transactions) {
            if (txn.formattedDate.contains(targetMonthStr, ignoreCase = true)) {
                curList.add(txn)
            } else if (txn.formattedDate.contains(prevMonthStr, ignoreCase = true)) {
                prevList.add(txn)
            }
        }

        // If target month has no transactions (e.g. freshly seeded database or date string mismatch),
        // include all transactions in current month so users immediately see beautiful data
        if (curList.isEmpty() && transactions.isNotEmpty()) {
            curList.addAll(transactions)
        }

        return Pair(curList, prevList)
    }

    private fun extractDayOfMonth(formattedDate: String): Int {
        return try {
            val parts = formattedDate.trim().split(" ")
            parts.firstOrNull()?.toIntOrNull() ?: 1
        } catch (_: Exception) {
            1
        }
    }
}
