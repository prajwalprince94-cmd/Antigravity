package com.prajwalprince94.expensetracker.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * AppWidgetProvider receiver for the Jetpack Glance home screen widget.
 */
class ExpenseWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ExpenseGlanceWidget()
}
