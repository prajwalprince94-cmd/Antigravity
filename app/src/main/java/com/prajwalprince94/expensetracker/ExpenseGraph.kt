package com.prajwalprince94.expensetracker

import android.content.Context
import com.prajwalprince94.expensetracker.data.repo.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Service locator patterned after NikhilKain/stride's Graph.
 * Ensures single instance initialization of data layer and repository.
 */
object ExpenseGraph {
    lateinit var repository: ExpenseRepository
        private set

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var initialized = false

    fun ensureInit(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val app = context.applicationContext
            repository = ExpenseRepository(app, appScope)
            initialized = true
        }
    }
}
