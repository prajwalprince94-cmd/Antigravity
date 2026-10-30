package com.prajwalprince94.expensetracker.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.prajwalprince94.expensetracker.MainActivity
import com.prajwalprince94.expensetracker.data.db.ExpenseDatabaseHelper
import com.prajwalprince94.expensetracker.ui.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Native Jetpack Glance Home Screen Widget following platform canonical layout principles.
 * Displays real-time net balance, income/expense breakdown, and 1-tap quick action launchers.
 */
class ExpenseGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Read latest offline transactions & budgets directly from SQLite helper
        val (netBalancePaise, totalIncomePaise, totalExpensePaise, budgetPercent) = withContext(Dispatchers.IO) {
            try {
                val dbHelper = ExpenseDatabaseHelper(context.applicationContext)
                val transactions = dbHelper.getAllTransactions()
                var income = 0L
                var expense = 0L
                for (txn in transactions) {
                    when (txn.type) {
                        TransactionType.INCOME -> income += txn.amountPaise
                        TransactionType.EXPENSE -> expense += txn.amountPaise
                        TransactionType.TRANSFER -> {}
                    }
                }
                val net = income - expense

                val budgets = dbHelper.getAllBudgets()
                val totalLimit = budgets.sumOf { it.limitPaise }
                val totalSpent = budgets.sumOf { it.spentPaise }
                val percent = if (totalLimit > 0L) ((totalSpent.toDouble() / totalLimit) * 100).toInt() else 0

                Quadruple(net, income, expense, percent)
            } catch (e: Exception) {
                Quadruple(0L, 0L, 0L, 0)
            }
        }

        val launchAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val quickAddIntent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_QUICK_ADD
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val scanIntent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_SCAN_RECEIPT
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val smsSyncIntent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_SMS_SYNC
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        provideContent {
            GlanceTheme {
                val backgroundColor = Color(0xFF14171C)
                val cardSurfaceColor = Color(0xFF1E2229)
                val primaryColor = Color(0xFF6C9EFF)
                val incomeColor = Color(0xFF4ADE80)
                val expenseColor = Color(0xFFF87171)
                val textPrimary = Color(0xFFFFFFFF)
                val textSecondary = Color(0xFF9CA3AF)

                val formattedNet = "₹%,.2f".format(Locale.US, netBalancePaise / 100.0)
                val formattedIncome = "+₹%,.0f".format(Locale.US, totalIncomePaise / 100.0)
                val formattedExpense = "-₹%,.0f".format(Locale.US, totalExpensePaise / 100.0)

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .cornerRadius(22.dp)
                        .padding(14.dp)
                        .clickable(actionStartActivity(launchAppIntent))
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Header: App Title & Budget Progress Badge
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EXPENSE TRACKER",
                                style = TextStyle(
                                    color = glanceColor(primaryColor),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )

                            Box(
                                modifier = GlanceModifier
                                    .background(cardSurfaceColor)
                                    .cornerRadius(8.dp)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (budgetPercent > 0) "$budgetPercent% Budget" else "Offline",
                                    style = TextStyle(
                                        color = glanceColor(if (budgetPercent > 85) expenseColor else primaryColor),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(6.dp))

                        // Net Balance Display
                        Text(
                            text = formattedNet,
                            style = TextStyle(
                                color = glanceColor(textPrimary),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(2.dp))

                        // Quick Stats Subtitle (Income & Expense)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formattedIncome,
                                style = TextStyle(
                                    color = glanceColor(incomeColor),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Text(
                                text = formattedExpense,
                                style = TextStyle(
                                    color = glanceColor(expenseColor),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.defaultWeight())

                        // 1-Tap Action Buttons Row
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                text = "+ Expense",
                                onClick = actionStartActivity(quickAddIntent),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = glanceColor(expenseColor),
                                    contentColor = glanceColor(Color.White)
                                ),
                                modifier = GlanceModifier.defaultWeight().height(36.dp)
                            )

                            Spacer(modifier = GlanceModifier.width(6.dp))

                            Button(
                                text = "Scan OCR",
                                onClick = actionStartActivity(scanIntent),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = glanceColor(cardSurfaceColor),
                                    contentColor = glanceColor(textPrimary)
                                ),
                                modifier = GlanceModifier.defaultWeight().height(36.dp)
                            )

                            Spacer(modifier = GlanceModifier.width(6.dp))

                            Button(
                                text = "SMS Sync",
                                onClick = actionStartActivity(smsSyncIntent),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = glanceColor(primaryColor),
                                    contentColor = glanceColor(Color.White)
                                ),
                                modifier = GlanceModifier.defaultWeight().height(36.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        suspend fun update(context: Context) {
            withContext(Dispatchers.IO) {
                try {
                    ExpenseGlanceWidget().updateAll(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

/**
 * Creates a Glance ColorProvider using the factory function to avoid constructor errors.
 */
private fun glanceColor(color: Color): androidx.glance.unit.ColorProvider =
    androidx.glance.color.ColorProvider(day = color, night = color)

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
