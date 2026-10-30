package com.prajwalprince94.expensetracker.ui.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.data.analytics.CategorySpendData
import com.prajwalprince94.expensetracker.data.analytics.DailySpendPoint
import com.prajwalprince94.expensetracker.data.analytics.FinancialHealthReport
import com.prajwalprince94.expensetracker.data.analytics.FinancialInsight
import com.prajwalprince94.expensetracker.data.analytics.HealthTier
import com.prajwalprince94.expensetracker.data.analytics.InsightType
import com.prajwalprince94.expensetracker.ui.components.AnimatedCounter
import com.prajwalprince94.expensetracker.ui.components.GlassCard
import com.prajwalprince94.expensetracker.ui.components.IconRegistry
import com.prajwalprince94.expensetracker.ui.components.SectionHeader
import com.prajwalprince94.expensetracker.ui.components.StatusPill
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.entrance
import com.prajwalprince94.expensetracker.ui.components.rememberAppHaptics
import com.prajwalprince94.expensetracker.ui.theme.ButtonShape
import com.prajwalprince94.expensetracker.ui.theme.CardShape
import com.prajwalprince94.expensetracker.ui.theme.ChipShape
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen
import com.prajwalprince94.expensetracker.ui.theme.PillShape
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Hero Gauge rendering the 0-100 Financial Health Score with circular gradient arc,
 * tier badge, and 4 pillar sub-scores.
 */
@Composable
fun HealthScoreHeroGauge(
    report: FinancialHealthReport,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppHaptics()
    val animatedProgress = remember { Animatable(0f) }
    val tierColor = IconRegistry.parseColor(report.tier.colorHex)

    LaunchedEffect(report.overallScore) {
        animatedProgress.animateTo(
            targetValue = report.overallScore / 100f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 20.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FINANCIAL HEALTH SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = PillShape,
                    color = tierColor.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = report.tier.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = tierColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Circular Radial Arc Gauge
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                val sweepTarget = 260f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidthPx = 16.dp.toPx()
                    val arcSize = size.width - strokeWidthPx
                    val startAngle = 140f

                    // Background Track
                    drawArc(
                        color = trackColor,
                        startAngle = startAngle,
                        sweepAngle = sweepTarget,
                        useCenter = false,
                        topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )

                    // Active Progress Gradient Arc
                    val activeSweep = sweepTarget * animatedProgress.value
                    if (activeSweep > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                0.0f to Color(0xFFEF4444),
                                0.25f to Color(0xFFF59E0B),
                                0.5f to Color(0xFF14B8A6),
                                1.0f to Color(0xFF10B981)
                            ),
                            startAngle = startAngle,
                            sweepAngle = activeSweep,
                            useCenter = false,
                            topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center Score & Label
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val scoreDisplay = (report.overallScore * animatedProgress.value / (report.overallScore / 100f).coerceAtLeast(0.01f)).toInt().coerceIn(0, 100)
                    AnimatedCounter(
                        text = "$scoreDisplay",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Default
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "out of 100",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4 Pillar Breakdown Sub-Scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PillarScoreChip(
                    label = "Savings",
                    score = report.savingsScore,
                    maxScore = 35,
                    color = IncomeGreen,
                    modifier = Modifier.weight(1f)
                )
                PillarScoreChip(
                    label = "Budgets",
                    score = report.budgetAdherenceScore,
                    maxScore = 25,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
                PillarScoreChip(
                    label = "Fixed Bills",
                    score = report.commitmentScore,
                    maxScore = 20,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                PillarScoreChip(
                    label = "Stability",
                    score = report.stabilityScore,
                    maxScore = 20,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PillarScoreChip(
    label: String,
    score: Int,
    maxScore: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = ChipShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score/$maxScore",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

/**
 * Month-Over-Month Comparison Card showing spending variance, delta badges,
 * and day-by-day comparison chart.
 */
@Composable
fun MonthOverMonthComparisonCard(
    report: FinancialHealthReport,
    modifier: Modifier = Modifier
) {
    val isLowerSpend = report.momExpenseChangePercent <= 0f
    val deltaColor = if (isLowerSpend) IncomeGreen else ExpenseRed

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 18.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MONTH-OVER-MONTH TREND",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = report.formattedExpense,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = PillShape,
                    color = deltaColor.copy(alpha = 0.16f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLowerSpend) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = deltaColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "%.1f%% vs last mo".format(kotlin.math.abs(report.momExpenseChangePercent)),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = deltaColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Comparative Dual-Line Chart (Current vs Previous Month)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                val width = size.width
                val height = size.height
                val points = report.dailySpendTrend.take(report.daysElapsedInMonth)
                if (points.isEmpty()) return@Canvas

                val maxVal = report.dailySpendTrend.maxOfOrNull {
                    maxOf(it.currentMonthAmountPaise, it.previousMonthAmountPaise)
                }?.toFloat()?.coerceAtLeast(100_00f) ?: 100_00f

                val stepX = width / points.size.coerceAtLeast(1)

                // Previous Month Dashed / Muted Line
                val prevPath = Path()
                points.forEachIndexed { i, pt ->
                    val x = i * stepX
                    val y = height - ((pt.previousMonthAmountPaise.toFloat() / maxVal) * (height - 15f)) - 5f
                    if (i == 0) prevPath.moveTo(x, y) else prevPath.lineTo(x, y)
                }
                drawPath(
                    path = prevPath,
                    color = Color.Gray.copy(alpha = 0.45f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Current Month Vibrant Primary Line
                val curPath = Path()
                points.forEachIndexed { i, pt ->
                    val x = i * stepX
                    val y = height - ((pt.currentMonthAmountPaise.toFloat() / maxVal) * (height - 15f)) - 5f
                    if (i == 0) curPath.moveTo(x, y) else curPath.lineTo(x, y)
                }
                drawPath(
                    path = curPath,
                    color = deltaColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(deltaColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Current Month (${report.formattedExpense})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Gray.copy(alpha = 0.45f)))
                    Spacer(modifier = Modifier.width(6.dp))
                    val prevExp = "₹%,.2f".format(Locale.US, report.previousMonthExpensePaise / 100.0)
                    Text(
                        text = "Prev Month ($prevExp)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Burn-Rate & Month-End Forecast Card.
 */
@Composable
fun BurnRateForecastCard(
    report: FinancialHealthReport,
    modifier: Modifier = Modifier
) {
    val progressElapsed = (report.daysElapsedInMonth.toFloat() / report.daysInMonth.toFloat()).coerceIn(0f, 1f)
    val isOnTrack = report.projectedSurplusDeficitPaise >= 0L
    val trackColor = if (isOnTrack) IncomeGreen else ExpenseRed

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 18.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BURN RATE & FORECAST",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = PillShape,
                    color = trackColor.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = if (isOnTrack) "On Track to Save" else "Deficit Projected",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = trackColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Daily Burn Rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${report.formattedDailyBurn} / day",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Month-End Forecast",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = report.formattedProjectedEndSpend,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = trackColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar showing time elapsed in month
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Day ${report.daysElapsedInMonth} of ${report.daysInMonth}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${report.daysRemainingInMonth} days remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progressElapsed },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(PillShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            }
        }
    }
}

/**
 * Interactive Donut Chart for Category Spend Distribution.
 * Tapping a slice highlights it with spring animation and shows its detail in the center.
 */
@Composable
fun InteractiveCategoryDonutChart(
    categories: List<CategorySpendData>,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppHaptics()
    var selectedCategoryName by remember { mutableStateOf<String?>(categories.firstOrNull()?.category) }

    val selectedData = categories.find { it.category == selectedCategoryName } ?: categories.firstOrNull()

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 18.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CATEGORY DISTRIBUTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${categories.size} Categories",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Donut Chart Canvas
            Box(
                modifier = Modifier.size(190.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(categories) {
                            detectTapGestures { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val dx = offset.x - center.x
                                val dy = offset.y - center.y
                                var angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (angle < 0) angle += 360f

                                var curAngle = 0f
                                for (cat in categories) {
                                    val sweep = (cat.percentage / 100f) * 360f
                                    if (angle >= curAngle && angle < curAngle + sweep) {
                                        haptics.selection()
                                        selectedCategoryName = cat.category
                                        break
                                    }
                                    curAngle += sweep
                                }
                            }
                        }
                ) {
                    val strokeWidth = 24.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    var startAngle = -90f

                    categories.forEach { cat ->
                        val isSelected = cat.category == selectedData?.category
                        val sweep = (cat.percentage / 100f) * 360f
                        val color = IconRegistry.parseColor(cat.colorHex)
                        val extraStroke = if (isSelected) 6.dp.toPx() else 0f

                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweep.coerceAtLeast(1.5f),
                            useCenter = false,
                            topLeft = Offset((strokeWidth - extraStroke) / 2f, (strokeWidth - extraStroke) / 2f),
                            size = Size(arcSize + extraStroke, arcSize + extraStroke),
                            style = Stroke(width = strokeWidth + extraStroke, cap = StrokeCap.Butt)
                        )
                        startAngle += sweep
                    }
                }

                // Center Display
                selectedData?.let { sel ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = sel.category,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = sel.formattedAmount,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = IconRegistry.parseColor(sel.colorHex)
                        )
                        Text(
                            text = "%.1f%%".format(sel.percentage),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat.category == selectedData?.category
                    val color = IconRegistry.parseColor(cat.colorHex)

                    Surface(
                        shape = ChipShape,
                        color = if (isSelected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.bouncyClickable(scaleDown = 0.92f) {
                            haptics.selection()
                            selectedCategoryName = cat.category
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${cat.category} (${cat.percentage.toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Actionable Insights Carousel displaying personalized, AI-level recommendations.
 */
@Composable
fun ActionableInsightsSection(
    insights: List<FinancialInsight>,
    modifier: Modifier = Modifier
) {
    if (insights.isEmpty()) return

    Column(modifier = modifier) {
        SectionHeader(title = "Smart Financial Insights")
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(insights) { insight ->
                val (icon, tint) = when (insight.type) {
                    InsightType.POSITIVE -> Pair(Icons.Default.CheckCircle, IncomeGreen)
                    InsightType.WARNING -> Pair(Icons.Default.Warning, ExpenseRed)
                    InsightType.TIP -> Pair(Icons.Default.Lightbulb, Color(0xFFF59E0B))
                    InsightType.INFO -> Pair(Icons.Default.Info, Color(0xFF3B82F6))
                }

                GlassCard(
                    modifier = Modifier.width(260.dp),
                    contentPadding = 16.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(tint.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = tint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Surface(
                                shape = PillShape,
                                color = tint.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = insight.impactBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = tint,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = insight.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = insight.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
