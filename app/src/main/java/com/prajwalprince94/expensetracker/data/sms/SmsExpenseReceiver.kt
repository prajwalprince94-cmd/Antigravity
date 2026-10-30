package com.prajwalprince94.expensetracker.data.sms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.prajwalprince94.expensetracker.MainActivity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Android BroadcastReceiver that listens for incoming bank debit/credit SMS messages.
 * When a transaction is detected, it posts a high-priority heads-up notification
 * allowing 1-tap expense logging.
 */
class SmsExpenseReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "channel_sms_expenses"
        const val CHANNEL_NAME = "Automated SMS Expenses"

        private val _liveDetectedExpenses = MutableSharedFlow<ParsedSmsExpense>(extraBufferCapacity = 10)
        val liveDetectedExpenses = _liveDetectedExpenses.asSharedFlow()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        try {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val sender = sms.displayOriginatingAddress ?: ""
                val body = sms.displayMessageBody ?: ""
                val timestamp = sms.timestampMillis

                if (SmsExpenseParser.isFinancialSms(sender, body)) {
                    val parsed = SmsExpenseParser.parse(sender, body, timestamp)
                    if (parsed != null && parsed.amountPaise > 0L) {
                        _liveDetectedExpenses.tryEmit(parsed)
                        showExpenseNotification(context, parsed)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showExpenseNotification(context: Context, expense: ParsedSmsExpense) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when a bank transaction or UPI debit is detected"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            expense.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("New Expense: ${expense.formattedAmount}")
            .setContentText("${expense.merchantOrBeneficiary} • ${expense.displayAccount}")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Detected ${expense.type.name.lowercase().replaceFirstChar { it.uppercase() }} of ${expense.formattedAmount} at ${expense.merchantOrBeneficiary} from ${expense.displayAccount}.\nTap to view in Expense Tracker."
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(expense.id.hashCode(), notification)
    }
}
