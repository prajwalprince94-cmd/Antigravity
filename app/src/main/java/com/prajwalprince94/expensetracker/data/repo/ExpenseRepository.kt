package com.prajwalprince94.expensetracker.data.repo

import android.content.Context
import com.prajwalprince94.expensetracker.data.db.ExpenseDatabaseHelper
import com.prajwalprince94.expensetracker.data.model.CategoryBudgetItem
import com.prajwalprince94.expensetracker.ui.model.AppPreferences
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Single source of truth for all Expense Tracker business logic and persistence.
 * Dispatches SQLite operations on IO coroutine dispatcher and updates StateFlows reactively.
 */
class ExpenseRepository(
    context: Context,
    private val scope: CoroutineScope
) {
    private val dbHelper = ExpenseDatabaseHelper(context.applicationContext)

    private val _transactions = MutableStateFlow<List<TransactionItem>>(emptyList())
    val transactions: StateFlow<List<TransactionItem>> = _transactions.asStateFlow()

    private val _budgets = MutableStateFlow<List<CategoryBudgetItem>>(emptyList())
    val budgets: StateFlow<List<CategoryBudgetItem>> = _budgets.asStateFlow()

    private val _preferences = MutableStateFlow(AppPreferences())
    val preferences: StateFlow<AppPreferences> = _preferences.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            loadInitialData()
        }
    }

    private fun loadInitialData() {
        val txns = dbHelper.getAllTransactions()
        dbHelper.recalculateBudgetsSpent(txns)
        val bdgts = dbHelper.getAllBudgets()
        val prefs = dbHelper.getPreferences()

        _transactions.value = txns
        _budgets.value = bdgts
        _preferences.value = prefs
        _isLoaded.value = true
    }

    suspend fun addTransaction(txn: TransactionItem): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.insertTransaction(txn)
        if (success) {
            val updatedTxns = dbHelper.getAllTransactions()
            dbHelper.recalculateBudgetsSpent(updatedTxns)
            val updatedBudgets = dbHelper.getAllBudgets()
            _transactions.value = updatedTxns
            _budgets.value = updatedBudgets
        }
        success
    }

    suspend fun deleteTransaction(id: String): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.deleteTransaction(id)
        if (success) {
            val updatedTxns = dbHelper.getAllTransactions()
            dbHelper.recalculateBudgetsSpent(updatedTxns)
            val updatedBudgets = dbHelper.getAllBudgets()
            _transactions.value = updatedTxns
            _budgets.value = updatedBudgets
        }
        success
    }

    suspend fun clearTransactions(): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.clearAllTransactions()
        if (success) {
            val updatedTxns = dbHelper.getAllTransactions()
            dbHelper.recalculateBudgetsSpent(updatedTxns)
            val updatedBudgets = dbHelper.getAllBudgets()
            _transactions.value = updatedTxns
            _budgets.value = updatedBudgets
        }
        success
    }

    suspend fun updateBudgetLimit(id: String, newLimitPaise: Long): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.updateBudgetLimit(id, newLimitPaise)
        if (success) {
            _budgets.value = dbHelper.getAllBudgets()
        }
        success
    }

    suspend fun savePreferences(newPrefs: AppPreferences) = withContext(Dispatchers.IO) {
        dbHelper.savePreferences(newPrefs)
        _preferences.value = newPrefs
    }

    suspend fun resetDatabase() = withContext(Dispatchers.IO) {
        dbHelper.resetToDefaults()
        loadInitialData()
    }
}
