package com.prajwalprince94.expensetracker

import android.app.Application

/**
 * Application class ensuring ExpenseGraph and local SQLite persistence
 * are initialized before any UI frames render.
 */
class ExpenseTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ExpenseGraph.ensureInit(this)
    }
}
