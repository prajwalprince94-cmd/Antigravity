package com.prajwalprince94.expensetracker.data.repo

import android.content.Context
import com.prajwalprince94.expensetracker.data.db.ExpenseDatabaseHelper
import com.prajwalprince94.expensetracker.data.model.AccountItem
import com.prajwalprince94.expensetracker.data.model.CategoryBudgetItem
import com.prajwalprince94.expensetracker.data.model.CategoryItem
import com.prajwalprince94.expensetracker.data.model.SubscriptionItem
import com.prajwalprince94.expensetracker.ui.model.AppPreferences
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionType
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

    private val _accounts = MutableStateFlow<List<AccountItem>>(emptyList())
    val accounts: StateFlow<List<AccountItem>> = _accounts.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    private val _subscriptions = MutableStateFlow<List<SubscriptionItem>>(emptyList())
    val subscriptions: StateFlow<List<SubscriptionItem>> = _subscriptions.asStateFlow()

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
        val accs = dbHelper.getAllAccounts()
        val cats = dbHelper.getAllCategories()
        val subs = dbHelper.getAllSubscriptions()

        _transactions.value = txns
        _budgets.value = bdgts
        _preferences.value = prefs
        _accounts.value = accs
        _categories.value = cats
        _subscriptions.value = subs
        _isLoaded.value = true
    }

    // --- Transactions ---

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

    // --- Accounts ---

    suspend fun addAccount(account: AccountItem): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.insertAccount(account)
        if (success) {
            _accounts.value = dbHelper.getAllAccounts()
        }
        success
    }

    suspend fun updateAccount(account: AccountItem): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.updateAccount(account)
        if (success) {
            _accounts.value = dbHelper.getAllAccounts()
        }
        success
    }

    suspend fun deleteAccount(id: String): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.deleteAccount(id)
        if (success) {
            _accounts.value = dbHelper.getAllAccounts()
        }
        success
    }

    /**
     * Computes the live real-time balance for an account by combining initial balance with transactions.
     */
    fun calculateAccountLiveBalance(account: AccountItem, txns: List<TransactionItem>): Long {
        val credited = txns
            .filter { it.type == TransactionType.INCOME && it.account.equals(account.name, ignoreCase = true) }
            .sumOf { it.amountPaise }
        val debited = txns
            .filter { it.type == TransactionType.EXPENSE && it.account.equals(account.name, ignoreCase = true) }
            .sumOf { it.amountPaise }
        return (account.initialBalancePaise + credited - debited).coerceAtLeast(0L)
    }

    // --- Categories ---

    suspend fun addCategory(category: CategoryItem): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.insertCategory(category)
        if (success) {
            _categories.value = dbHelper.getAllCategories()
        }
        success
    }

    suspend fun deleteCategory(id: String): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.deleteCategory(id)
        if (success) {
            _categories.value = dbHelper.getAllCategories()
        }
        success
    }

    // --- Budgets ---

    suspend fun updateBudgetLimit(id: String, newLimitPaise: Long): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.updateBudgetLimit(id, newLimitPaise)
        if (success) {
            _budgets.value = dbHelper.getAllBudgets()
        }
        success
    }

    // --- Subscriptions ---

    suspend fun addSubscription(sub: SubscriptionItem): Boolean = withContext(Dispatchers.IO) {
        val rowId = dbHelper.insertSubscription(sub)
        val success = rowId != -1L
        if (success) {
            _subscriptions.value = dbHelper.getAllSubscriptions()
        }
        success
    }

    suspend fun updateSubscription(sub: SubscriptionItem): Boolean = withContext(Dispatchers.IO) {
        val count = dbHelper.updateSubscription(sub)
        val success = count > 0
        if (success) {
            _subscriptions.value = dbHelper.getAllSubscriptions()
        }
        success
    }

    suspend fun deleteSubscription(id: String): Boolean = withContext(Dispatchers.IO) {
        val count = dbHelper.deleteSubscription(id)
        val success = count > 0
        if (success) {
            _subscriptions.value = dbHelper.getAllSubscriptions()
        }
        success
    }

    suspend fun logSubscriptionPayment(subId: String): TransactionItem? = withContext(Dispatchers.IO) {
        val txn = dbHelper.logSubscriptionPayment(subId)
        if (txn != null) {
            val updatedTxns = dbHelper.getAllTransactions()
            dbHelper.recalculateBudgetsSpent(updatedTxns)
            _transactions.value = updatedTxns
            _budgets.value = dbHelper.getAllBudgets()
            _subscriptions.value = dbHelper.getAllSubscriptions()
        }
        txn
    }

    // --- Preferences & Reset ---

    suspend fun savePreferences(newPrefs: AppPreferences) = withContext(Dispatchers.IO) {
        dbHelper.savePreferences(newPrefs)
        _preferences.value = newPrefs
    }

    suspend fun resetDatabase() = withContext(Dispatchers.IO) {
        dbHelper.resetToDefaults()
        loadInitialData()
    }
}
