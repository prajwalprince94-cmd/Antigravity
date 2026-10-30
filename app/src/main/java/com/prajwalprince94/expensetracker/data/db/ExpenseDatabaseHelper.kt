package com.prajwalprince94.expensetracker.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.prajwalprince94.expensetracker.data.model.AccountItem
import com.prajwalprince94.expensetracker.data.model.CategoryBudgetItem
import com.prajwalprince94.expensetracker.data.model.CategoryItem
import com.prajwalprince94.expensetracker.ui.model.AppPalette
import com.prajwalprince94.expensetracker.ui.model.AppPreferences
import com.prajwalprince94.expensetracker.ui.model.ColorStyle
import com.prajwalprince94.expensetracker.ui.model.ThemeMode
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionType

/**
 * Local offline SQLite database for Expense Tracker.
 * Manages atomic ACID transactions, accounts, categories, category budgets, and preferences.
 */
class ExpenseDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "expense_tracker.db"
        const val DATABASE_VERSION = 2

        // Transactions table
        const val TABLE_TRANSACTIONS = "transactions"
        const val COL_TXN_ID = "id"
        const val COL_TXN_TITLE = "title"
        const val COL_TXN_CATEGORY = "category"
        const val COL_TXN_ACCOUNT = "account"
        const val COL_TXN_AMOUNT_PAISE = "amount_paise"
        const val COL_TXN_TYPE = "type"
        const val COL_TXN_DATE = "formatted_date"
        const val COL_TXN_NOTE = "note"
        const val COL_TXN_ICON = "icon_name"
        const val COL_TXN_TIMESTAMP = "timestamp"

        // Accounts table
        const val TABLE_ACCOUNTS = "accounts"
        const val COL_ACC_ID = "id"
        const val COL_ACC_NAME = "name"
        const val COL_ACC_TYPE = "type"
        const val COL_ACC_INITIAL_BALANCE = "initial_balance_paise"
        const val COL_ACC_COLOR = "color_hex"
        const val COL_ACC_ICON = "icon_key"

        // Categories table
        const val TABLE_CATEGORIES = "categories"
        const val COL_CAT_ID = "id"
        const val COL_CAT_NAME = "name"
        const val COL_CAT_TYPE = "type"
        const val COL_CAT_COLOR = "color_hex"
        const val COL_CAT_ICON = "icon_key"
        const val COL_CAT_IS_SYSTEM = "is_system"

        // Budgets table
        const val TABLE_BUDGETS = "budgets"
        const val COL_BUDGET_ID = "id"
        const val COL_BUDGET_CATEGORY = "category"
        const val COL_BUDGET_SPENT = "spent_paise"
        const val COL_BUDGET_LIMIT = "limit_paise"
        const val COL_BUDGET_ICON = "icon_name"

        // Preferences table
        const val TABLE_PREFERENCES = "preferences"
        const val COL_PREF_KEY = "key"
        const val COL_PREF_VAL = "val"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_TRANSACTIONS (
                $COL_TXN_ID TEXT PRIMARY KEY,
                $COL_TXN_TITLE TEXT NOT NULL,
                $COL_TXN_CATEGORY TEXT NOT NULL,
                $COL_TXN_ACCOUNT TEXT NOT NULL,
                $COL_TXN_AMOUNT_PAISE INTEGER NOT NULL,
                $COL_TXN_TYPE TEXT NOT NULL,
                $COL_TXN_DATE TEXT NOT NULL,
                $COL_TXN_NOTE TEXT,
                $COL_TXN_ICON TEXT,
                $COL_TXN_TIMESTAMP INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_BUDGETS (
                $COL_BUDGET_ID TEXT PRIMARY KEY,
                $COL_BUDGET_CATEGORY TEXT NOT NULL UNIQUE,
                $COL_BUDGET_SPENT INTEGER NOT NULL,
                $COL_BUDGET_LIMIT INTEGER NOT NULL,
                $COL_BUDGET_ICON TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_PREFERENCES (
                $COL_PREF_KEY TEXT PRIMARY KEY,
                $COL_PREF_VAL TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Seed initial sample transactions
        TransactionItem.defaultSampleList().forEachIndexed { index, txn ->
            val cv = ContentValues().apply {
                put(COL_TXN_ID, txn.id)
                put(COL_TXN_TITLE, txn.title)
                put(COL_TXN_CATEGORY, txn.category)
                put(COL_TXN_ACCOUNT, txn.account)
                put(COL_TXN_AMOUNT_PAISE, txn.amountPaise)
                put(COL_TXN_TYPE, txn.type.name)
                put(COL_TXN_DATE, txn.formattedDate)
                put(COL_TXN_NOTE, txn.note)
                put(COL_TXN_ICON, txn.iconName)
                put(COL_TXN_TIMESTAMP, System.currentTimeMillis() - (index * 86400000L))
            }
            db.insert(TABLE_TRANSACTIONS, null, cv)
        }

        // Seed initial category budgets
        CategoryBudgetItem.defaultList().forEach { budget ->
            val cv = ContentValues().apply {
                put(COL_BUDGET_ID, budget.id)
                put(COL_BUDGET_CATEGORY, budget.category)
                put(COL_BUDGET_SPENT, budget.spentPaise)
                put(COL_BUDGET_LIMIT, budget.limitPaise)
                put(COL_BUDGET_ICON, budget.iconName)
            }
            db.insert(TABLE_BUDGETS, null, cv)
        }

        ensureAccountsAndCategories(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        ensureAccountsAndCategories(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            ensureAccountsAndCategories(db)
        }
    }

    /**
     * Creates accounts and categories tables if not existing, and populates default seeds.
     */
    private fun ensureAccountsAndCategories(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_ACCOUNTS (
                $COL_ACC_ID TEXT PRIMARY KEY,
                $COL_ACC_NAME TEXT NOT NULL,
                $COL_ACC_TYPE TEXT NOT NULL,
                $COL_ACC_INITIAL_BALANCE INTEGER NOT NULL,
                $COL_ACC_COLOR TEXT,
                $COL_ACC_ICON TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES (
                $COL_CAT_ID TEXT PRIMARY KEY,
                $COL_CAT_NAME TEXT NOT NULL,
                $COL_CAT_TYPE TEXT NOT NULL,
                $COL_CAT_COLOR TEXT,
                $COL_CAT_ICON TEXT,
                $COL_CAT_IS_SYSTEM INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        // Seed accounts if table is empty
        val accCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ACCOUNTS", null)
        val accCount = if (accCursor.moveToFirst()) accCursor.getInt(0) else 0
        accCursor.close()

        if (accCount == 0) {
            AccountItem.defaultList().forEach { acc ->
                val cv = ContentValues().apply {
                    put(COL_ACC_ID, acc.id)
                    put(COL_ACC_NAME, acc.name)
                    put(COL_ACC_TYPE, acc.type)
                    put(COL_ACC_INITIAL_BALANCE, acc.initialBalancePaise)
                    put(COL_ACC_COLOR, acc.colorHex)
                    put(COL_ACC_ICON, acc.iconKey)
                }
                db.insert(TABLE_ACCOUNTS, null, cv)
            }
        }

        // Seed categories if table is empty
        val catCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CATEGORIES", null)
        val catCount = if (catCursor.moveToFirst()) catCursor.getInt(0) else 0
        catCursor.close()

        if (catCount == 0) {
            CategoryItem.defaultList().forEach { cat ->
                val cv = ContentValues().apply {
                    put(COL_CAT_ID, cat.id)
                    put(COL_CAT_NAME, cat.name)
                    put(COL_CAT_TYPE, cat.type.name)
                    put(COL_CAT_COLOR, cat.colorHex)
                    put(COL_CAT_ICON, cat.iconKey)
                    put(COL_CAT_IS_SYSTEM, if (cat.isSystem) 1 else 0)
                }
                db.insert(TABLE_CATEGORIES, null, cv)
            }
        }
    }

    // --- Accounts CRUD ---

    fun getAllAccounts(): List<AccountItem> {
        val list = mutableListOf<AccountItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_ACCOUNTS, null, null, null, null, null, "$COL_ACC_NAME ASC")
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow(COL_ACC_ID))
                val name = it.getString(it.getColumnIndexOrThrow(COL_ACC_NAME))
                val type = it.getString(it.getColumnIndexOrThrow(COL_ACC_TYPE))
                val initialBal = it.getLong(it.getColumnIndexOrThrow(COL_ACC_INITIAL_BALANCE))
                val color = it.getString(it.getColumnIndexOrThrow(COL_ACC_COLOR)) ?: "#0EA5E9"
                val icon = it.getString(it.getColumnIndexOrThrow(COL_ACC_ICON)) ?: "PhoneAndroid"

                list.add(
                    AccountItem(
                        id = id,
                        name = name,
                        type = type,
                        initialBalancePaise = initialBal,
                        colorHex = color,
                        iconKey = icon
                    )
                )
            }
        }
        return if (list.isNotEmpty()) list else AccountItem.defaultList()
    }

    fun insertAccount(acc: AccountItem): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_ACC_ID, acc.id)
            put(COL_ACC_NAME, acc.name)
            put(COL_ACC_TYPE, acc.type)
            put(COL_ACC_INITIAL_BALANCE, acc.initialBalancePaise)
            put(COL_ACC_COLOR, acc.colorHex)
            put(COL_ACC_ICON, acc.iconKey)
        }
        val res = db.insertWithOnConflict(TABLE_ACCOUNTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        return res != -1L
    }

    fun updateAccount(acc: AccountItem): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_ACC_NAME, acc.name)
            put(COL_ACC_TYPE, acc.type)
            put(COL_ACC_INITIAL_BALANCE, acc.initialBalancePaise)
            put(COL_ACC_COLOR, acc.colorHex)
            put(COL_ACC_ICON, acc.iconKey)
        }
        val rows = db.update(TABLE_ACCOUNTS, cv, "$COL_ACC_ID = ?", arrayOf(acc.id))
        return rows > 0
    }

    fun deleteAccount(id: String): Boolean {
        val db = writableDatabase
        val rows = db.delete(TABLE_ACCOUNTS, "$COL_ACC_ID = ?", arrayOf(id))
        return rows > 0
    }

    // --- Categories CRUD ---

    fun getAllCategories(): List<CategoryItem> {
        val list = mutableListOf<CategoryItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_CATEGORIES, null, null, null, null, null, "$COL_CAT_NAME ASC")
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow(COL_CAT_ID))
                val name = it.getString(it.getColumnIndexOrThrow(COL_CAT_NAME))
                val typeStr = it.getString(it.getColumnIndexOrThrow(COL_CAT_TYPE))
                val color = it.getString(it.getColumnIndexOrThrow(COL_CAT_COLOR)) ?: "#10B981"
                val icon = it.getString(it.getColumnIndexOrThrow(COL_CAT_ICON)) ?: "Fastfood"
                val isSystem = it.getInt(it.getColumnIndexOrThrow(COL_CAT_IS_SYSTEM)) == 1
                val type = runCatching { TransactionType.valueOf(typeStr) }.getOrDefault(TransactionType.EXPENSE)

                list.add(
                    CategoryItem(
                        id = id,
                        name = name,
                        type = type,
                        colorHex = color,
                        iconKey = icon,
                        isSystem = isSystem
                    )
                )
            }
        }
        return if (list.isNotEmpty()) list else CategoryItem.defaultList()
    }

    fun insertCategory(cat: CategoryItem): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_CAT_ID, cat.id)
            put(COL_CAT_NAME, cat.name)
            put(COL_CAT_TYPE, cat.type.name)
            put(COL_CAT_COLOR, cat.colorHex)
            put(COL_CAT_ICON, cat.iconKey)
            put(COL_CAT_IS_SYSTEM, if (cat.isSystem) 1 else 0)
        }
        val res = db.insertWithOnConflict(TABLE_CATEGORIES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        return res != -1L
    }

    fun deleteCategory(id: String): Boolean {
        val db = writableDatabase
        // Prevent deletion of system default categories
        val rows = db.delete(TABLE_CATEGORIES, "$COL_CAT_ID = ? AND $COL_CAT_IS_SYSTEM = 0", arrayOf(id))
        return rows > 0
    }

    // --- Transactions CRUD ---

    fun getAllTransactions(): List<TransactionItem> {
        val list = mutableListOf<TransactionItem>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSACTIONS,
            null,
            null,
            null,
            null,
            null,
            "$COL_TXN_TIMESTAMP DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow(COL_TXN_ID))
                val title = it.getString(it.getColumnIndexOrThrow(COL_TXN_TITLE))
                val category = it.getString(it.getColumnIndexOrThrow(COL_TXN_CATEGORY))
                val account = it.getString(it.getColumnIndexOrThrow(COL_TXN_ACCOUNT))
                val amountPaise = it.getLong(it.getColumnIndexOrThrow(COL_TXN_AMOUNT_PAISE))
                val typeStr = it.getString(it.getColumnIndexOrThrow(COL_TXN_TYPE))
                val date = it.getString(it.getColumnIndexOrThrow(COL_TXN_DATE))
                val note = it.getString(it.getColumnIndexOrThrow(COL_TXN_NOTE)) ?: ""
                val icon = it.getString(it.getColumnIndexOrThrow(COL_TXN_ICON)) ?: "ReceiptLong"
                val type = runCatching { TransactionType.valueOf(typeStr) }.getOrDefault(TransactionType.EXPENSE)

                list.add(
                    TransactionItem(
                        id = id,
                        title = title,
                        category = category,
                        account = account,
                        amountPaise = amountPaise,
                        type = type,
                        formattedDate = date,
                        note = note,
                        iconName = icon
                    )
                )
            }
        }
        return list
    }

    fun insertTransaction(txn: TransactionItem): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_TXN_ID, txn.id)
            put(COL_TXN_TITLE, txn.title)
            put(COL_TXN_CATEGORY, txn.category)
            put(COL_TXN_ACCOUNT, txn.account)
            put(COL_TXN_AMOUNT_PAISE, txn.amountPaise)
            put(COL_TXN_TYPE, txn.type.name)
            put(COL_TXN_DATE, txn.formattedDate)
            put(COL_TXN_NOTE, txn.note)
            put(COL_TXN_ICON, txn.iconName)
            put(COL_TXN_TIMESTAMP, System.currentTimeMillis())
        }
        val result = db.insertWithOnConflict(TABLE_TRANSACTIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        return result != -1L
    }

    fun deleteTransaction(id: String): Boolean {
        val db = writableDatabase
        val rows = db.delete(TABLE_TRANSACTIONS, "$COL_TXN_ID = ?", arrayOf(id))
        return rows > 0
    }

    fun clearAllTransactions(): Boolean {
        val db = writableDatabase
        val rows = db.delete(TABLE_TRANSACTIONS, null, null)
        return rows >= 0
    }

    // --- Budgets CRUD ---

    fun getAllBudgets(): List<CategoryBudgetItem> {
        val list = mutableListOf<CategoryBudgetItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_BUDGETS, null, null, null, null, null, "$COL_BUDGET_ID ASC")
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow(COL_BUDGET_ID))
                val category = it.getString(it.getColumnIndexOrThrow(COL_BUDGET_CATEGORY))
                val spent = it.getLong(it.getColumnIndexOrThrow(COL_BUDGET_SPENT))
                val limit = it.getLong(it.getColumnIndexOrThrow(COL_BUDGET_LIMIT))
                val icon = it.getString(it.getColumnIndexOrThrow(COL_BUDGET_ICON)) ?: "Fastfood"

                list.add(CategoryBudgetItem(id, category, spent, limit, icon))
            }
        }
        return if (list.isNotEmpty()) list else CategoryBudgetItem.defaultList()
    }

    fun updateBudgetLimit(id: String, newLimitPaise: Long): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_BUDGET_LIMIT, newLimitPaise)
        }
        val rows = db.update(TABLE_BUDGETS, cv, "$COL_BUDGET_ID = ?", arrayOf(id))
        return rows > 0
    }

    fun recalculateBudgetsSpent(transactions: List<TransactionItem>) {
        val db = writableDatabase
        val budgets = getAllBudgets()
        budgets.forEach { budget ->
            val spent = transactions
                .filter { it.type == TransactionType.EXPENSE && it.category.equals(budget.category, ignoreCase = true) }
                .sumOf { it.amountPaise }
            val cv = ContentValues().apply {
                put(COL_BUDGET_SPENT, spent)
            }
            db.update(TABLE_BUDGETS, cv, "$COL_BUDGET_ID = ?", arrayOf(budget.id))
        }
    }

    // --- Preferences Persistence ---

    fun getPreferences(): AppPreferences {
        val db = readableDatabase
        var themeMode = ThemeMode.SYSTEM
        var dynamicColor = true
        var amoled = false
        var colorStyle = ColorStyle.EXPRESSIVE
        var palette = AppPalette.EMERALD
        var ambientAurora = true
        var reduceMotion = false

        val cursor = db.query(TABLE_PREFERENCES, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val key = it.getString(it.getColumnIndexOrThrow(COL_PREF_KEY))
                val value = it.getString(it.getColumnIndexOrThrow(COL_PREF_VAL))
                when (key) {
                    "themeMode" -> runCatching { themeMode = ThemeMode.valueOf(value) }
                    "dynamicColor" -> dynamicColor = value.toBoolean()
                    "amoled" -> amoled = value.toBoolean()
                    "colorStyle" -> runCatching { colorStyle = ColorStyle.valueOf(value) }
                    "palette" -> runCatching { palette = AppPalette.valueOf(value) }
                    "ambientAurora" -> ambientAurora = value.toBoolean()
                    "reduceMotion" -> reduceMotion = value.toBoolean()
                }
            }
        }
        return AppPreferences(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            amoled = amoled,
            colorStyle = colorStyle,
            palette = palette,
            ambientAurora = ambientAurora,
            reduceMotion = reduceMotion
        )
    }

    fun savePreferences(prefs: AppPreferences) {
        val db = writableDatabase
        val map = mapOf(
            "themeMode" to prefs.themeMode.name,
            "dynamicColor" to prefs.dynamicColor.toString(),
            "amoled" to prefs.amoled.toString(),
            "colorStyle" to prefs.colorStyle.name,
            "palette" to prefs.palette.name,
            "ambientAurora" to prefs.ambientAurora.toString(),
            "reduceMotion" to prefs.reduceMotion.toString()
        )
        db.beginTransaction()
        try {
            map.forEach { (k, v) ->
                val cv = ContentValues().apply {
                    put(COL_PREF_KEY, k)
                    put(COL_PREF_VAL, v)
                }
                db.insertWithOnConflict(TABLE_PREFERENCES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun resetToDefaults() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_TRANSACTIONS")
        db.execSQL("DELETE FROM $TABLE_BUDGETS")
        db.execSQL("DELETE FROM $TABLE_PREFERENCES")
        db.execSQL("DELETE FROM $TABLE_ACCOUNTS")
        db.execSQL("DELETE FROM $TABLE_CATEGORIES")
        onCreate(db)
    }
}
