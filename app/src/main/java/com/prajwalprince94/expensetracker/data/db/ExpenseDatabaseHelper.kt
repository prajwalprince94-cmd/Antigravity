package com.prajwalprince94.expensetracker.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.prajwalprince94.expensetracker.data.model.CategoryBudgetItem
import com.prajwalprince94.expensetracker.ui.model.AppPalette
import com.prajwalprince94.expensetracker.ui.model.AppPreferences
import com.prajwalprince94.expensetracker.ui.model.ColorStyle
import com.prajwalprince94.expensetracker.ui.model.ThemeMode
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionType

/**
 * Local offline SQLite database for Expense Tracker.
 * Manages atomic ACID transactions, category budgets, and preferences.
 */
class ExpenseDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "expense_tracker.db"
        const val DATABASE_VERSION = 1

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
            CREATE TABLE $TABLE_TRANSACTIONS (
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
            CREATE TABLE $TABLE_BUDGETS (
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
            CREATE TABLE $TABLE_PREFERENCES (
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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGETS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PREFERENCES")
        onCreate(db)
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
        onCreate(db)
    }
}
