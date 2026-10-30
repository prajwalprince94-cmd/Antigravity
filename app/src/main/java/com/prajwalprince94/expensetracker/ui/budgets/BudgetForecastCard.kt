package com.prajwalprince94.expensetracker.ui.budgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.data.forecast.ActionableInsight
import com.prajwalprince94.expensetracker.data.forecast.CategoryForecast
import com.prajwalprince94.expensetracker.data.forecast.ForecastHealthStatus
import com.prajwalprince94.expensetracker.data.forecast.InsightSeverity
import com.prajwalprince94.expensetracker.data.forecast.OverallBudgetForecast
import com.prajwalprince94.expensetracker.ui.components.GlassCard
import com.prajwalprince94.expensetracker.ui.components.StatusPill
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.rememberAppHaptics
import com.prajwalprince94.expensetracker.ui.theme.ChipShape
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Premium Stride-styled predictive budget forecast and velocity card.
 * Renders daily burn rate, projected month-end spend, early burnout warning,
 * and AI-style actionable spending adjustments.
 */
@Composable
fun BudgetForecastCard(
    forecast: OverallBudgetForecast,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppHaptics()
    var expandedCategoryBreakdown by remember { mutableStateOf(false) }

    val statusColor = Color(forecast.healthStatus.colorHex)
    val statusBg = statusColor.copy(alpha = 0.15f)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentPadding = 18.dp
    ) {
        Column {
            // 1. Header with Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(statusBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "Forecast",
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SMART FORECAST & VELOCITY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )
                        Text(
                            text = "End-of-Month Projections",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                StatusPill(
                    text = forecast.healthStatus.label,
                    backgroundColor = statusBg,
                    contentColor = statusColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Velocity Metrics Grid (Current Run Rate vs Safe Cap vs Days Remaining)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Metric 1: Daily Run Rate
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CURRENT RUN RATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = forecast.formattedCurrentVelocity,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (forecast.healthStatus == ForecastHealthStatus.CRITICAL_BURNOUT ||
                            forecast.healthStatus == ForecastHealthStatus.EXCEEDED) ExpenseRed
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${forecast.daysElapsed} days elapsed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                // Metric 2: Safe Daily Cap
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SAFE DAILY CAP",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = forecast.formattedSafeAllowance,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = IncomeGreen
                    )
                    Text(
                        text = "${forecast.daysRemaining} days left",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                // Metric 3: Projected Total Month-End
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "PROJECTED SPEND",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = forecast.formattedProjectedSpend,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (forecast.projectedVariancePaise > 0) ExpenseRed else IncomeGreen
                    )
                    Text(
                        text = if (forecast.projectedVariancePaise > 0) {
                            "+${forecast.formattedProjectedOverrun} Overrun"
                        } else {
                            "${forecast.formattedProjectedSavings} Surplus"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (forecast.projectedVariancePaise > 0) ExpenseRed else IncomeGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Dual-Layer Projected Spending Progress Bar
            val currentProgress = (forecast.totalSpentPaise.toFloat() / forecast.totalLimitPaise.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
            val projectedProgress = (forecast.projectedTotalSpendPaise.toFloat() / forecast.totalLimitPaise.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                // Ghost Projected Bar (Extension)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(projectedProgress)
                        .height(8.dp)
                        .background(statusColor.copy(alpha = 0.35f))
                )
                // Solid Current Spent Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(currentProgress)
                        .height(8.dp)
                        .background(statusColor)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ₹%,.0f (%.0f%%)".format(forecast.totalSpentPaise / 100.0, currentProgress * 100),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Text(
                    text = "Projected: %.0f%% of Limit".format(forecast.projectedUtilizationPercent),
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            // 4. Early Burnout Warning Banner (if burnout expected before month end)
            if (forecast.daysUntilBurnout != null && forecast.daysUntilBurnout < forecast.daysRemaining) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ExpenseRed.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Alert",
                            tint = ExpenseRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            val burnoutStr = forecast.projectedBurnoutDate?.format(
                                DateTimeFormatter.ofPattern("MMM dd", Locale.US)
                            ) ?: "${forecast.daysUntilBurnout} days"
                            Text(
                                text = "Budget Exhaustion Warning: $burnoutStr",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                            Text(
                                text = "At your current velocity, budget runs out in ${forecast.daysUntilBurnout} days (${forecast.daysRemaining - forecast.daysUntilBurnout} days early). Trim spend by ₹%,.0f/day to make it last.".format(
                                    (forecast.currentDailyVelocityPaise - forecast.safeDailyAllowancePaise).coerceAtLeast(0L) / 100.0
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 5. Actionable AI Insights Carousel
            if (forecast.insights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "ACTIONABLE INSIGHTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(forecast.insights) { insight ->
                        InsightMiniCard(insight = insight)
                    }
                }
            }

            // 6. Category-by-Category Forecast Expandable Section
            if (forecast.categoryForecasts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bouncyClickable(scaleDown = 0.98f) {
                            haptics.light()
                            expandedCategoryBreakdown = !expandedCategoryBreakdown
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Run Rates (${forecast.categoryForecasts.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (expandedCategoryBreakdown) "Hide Details" else "View Velocity",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (expandedCategoryBreakdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = expandedCategoryBreakdown,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        forecast.categoryForecasts.forEach { cat ->
                            CategoryForecastRow(cat = cat)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightMiniCard(insight: ActionableInsight) {
    val borderColor = when (insight.severity) {
        InsightSeverity.CRITICAL -> ExpenseRed
        InsightSeverity.WARNING -> Color(0xFFFBBF24)
        InsightSeverity.POSITIVE -> IncomeGreen
        InsightSeverity.INFO -> Color(0xFF60A5FA)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.width(260.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (insight.severity == InsightSeverity.POSITIVE) Icons.Default.AutoAwesome else Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = borderColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
                if (insight.impactBadge.isNotBlank()) {
                    StatusPill(
                        text = insight.impactBadge,
                        backgroundColor = borderColor.copy(alpha = 0.15f),
                        contentColor = borderColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = insight.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            insight.suggestedAction?.let { action ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tip: $action",
                    style = MaterialTheme.typography.labelSmall,
                    color = borderColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun CategoryForecastRow(cat: CategoryForecast) {
    val statusColor = Color(cat.status.colorHex)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cat.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${cat.formattedSpent} of ${cat.formattedLimit} • Burn: ${cat.formattedVelocity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusPill(
                    text = cat.status.label,
                    backgroundColor = statusColor.copy(alpha = 0.15f),
                    contentColor = statusColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Proj. ${cat.formattedProjectedSpend}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
