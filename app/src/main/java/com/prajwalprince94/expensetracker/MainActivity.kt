package com.prajwalprince94.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prajwalprince94.expensetracker.data.model.CategoryBudgetItem
import com.prajwalprince94.expensetracker.data.update.AppUpdateInfo
import com.prajwalprince94.expensetracker.data.update.AppUpdateManager
import com.prajwalprince94.expensetracker.navigation.Screen
import com.prajwalprince94.expensetracker.ui.budgets.BudgetsScreen
import com.prajwalprince94.expensetracker.ui.components.BootSequence
import com.prajwalprince94.expensetracker.ui.components.FloatingBottomNavBar
import com.prajwalprince94.expensetracker.ui.components.LocalReduceMotion
import com.prajwalprince94.expensetracker.ui.components.QuickAddTransactionSheet
import com.prajwalprince94.expensetracker.ui.components.TransactionDetailSheet
import com.prajwalprince94.expensetracker.ui.components.UpdateBottomSheet
import com.prajwalprince94.expensetracker.ui.home.HomeScreen
import com.prajwalprince94.expensetracker.ui.model.AppPreferences
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionType
import com.prajwalprince94.expensetracker.ui.settings.SettingsScreen
import com.prajwalprince94.expensetracker.ui.theme.ExpenseTrackerTheme
import com.prajwalprince94.expensetracker.ui.transaction.TransactionsScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ExpenseGraph.ensureInit(this)
        enableEdgeToEdge()

        setContent {
            val repository = ExpenseGraph.repository
            val preferences by repository.preferences.collectAsState()
            val transactions by repository.transactions.collectAsState()
            val budgets by repository.budgets.collectAsState()
            val isLoaded by repository.isLoaded.collectAsState()

            var hasBooted by remember { mutableStateOf(false) }

            ExpenseTrackerTheme(preferences = preferences) {
                CompositionLocalProvider(LocalReduceMotion provides preferences.reduceMotion) {
                    Crossfade(
                        targetState = hasBooted,
                        animationSpec = tween(400),
                        label = "appBootCrossfade"
                    ) { booted ->
                        if (!booted) {
                            BootSequence(
                                isDataLoaded = isLoaded,
                                onBootComplete = { hasBooted = true }
                            )
                        } else {
                            MainApp(
                                preferences = preferences,
                                transactions = transactions,
                                budgets = budgets,
                                onAddTransaction = { txn ->
                                    ExpenseGraph.appScope.launch {
                                        repository.addTransaction(txn)
                                    }
                                },
                                onDeleteTransaction = { id ->
                                    ExpenseGraph.appScope.launch {
                                        repository.deleteTransaction(id)
                                    }
                                },
                                onUpdateBudgetLimit = { id, limit ->
                                    ExpenseGraph.appScope.launch {
                                        repository.updateBudgetLimit(id, limit)
                                    }
                                },
                                onUpdatePreferences = { newPrefs ->
                                    ExpenseGraph.appScope.launch {
                                        repository.savePreferences(newPrefs)
                                    }
                                },
                                onResetDatabase = {
                                    ExpenseGraph.appScope.launch {
                                        repository.resetDatabase()
                                    }
                                },
                                onClearTransactions = {
                                    ExpenseGraph.appScope.launch {
                                        repository.clearTransactions()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    preferences: AppPreferences,
    transactions: List<TransactionItem>,
    budgets: List<CategoryBudgetItem>,
    onAddTransaction: (TransactionItem) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onUpdateBudgetLimit: (String, Long) -> Unit,
    onUpdatePreferences: (AppPreferences) -> Unit,
    onResetDatabase: () -> Unit,
    onClearTransactions: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Modal Bottom Sheet States
    var showQuickAddSheet by remember { mutableStateOf(false) }
    var quickAddInitialType by remember { mutableStateOf(TransactionType.EXPENSE) }
    val quickAddSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTransactionForDetail by remember { mutableStateOf<TransactionItem?>(null) }
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var pendingUpdateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var showUpdateSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val updateInfo = AppUpdateManager.checkForUpdates(BuildConfig.VERSION_NAME)
            if (updateInfo.isUpdateAvailable) {
                pendingUpdateInfo = updateInfo
                showUpdateSheet = true
            }
        } catch (_: Exception) {}
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            FloatingBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        // Stride fluid horizontal slide + fade transitions
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                enterTransition = {
                    slideInHorizontally(tween(340)) { it / 6 } + fadeIn(tween(240))
                },
                exitTransition = {
                    slideOutHorizontally(tween(340)) { -it / 8 } + fadeOut(tween(180))
                },
                popEnterTransition = {
                    slideInHorizontally(tween(320)) { -it / 8 } + fadeIn(tween(220))
                },
                popExitTransition = {
                    slideOutHorizontally(tween(320)) { it / 6 } + fadeOut(tween(180))
                }
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        transactions = transactions,
                        onOpenQuickAdd = { type ->
                            quickAddInitialType = type
                            showQuickAddSheet = true
                        },
                        onNavigateToTransactions = {
                            navController.navigate(Screen.Transactions.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onSelectTransaction = { txn ->
                            selectedTransactionForDetail = txn
                        },
                        onShowMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }

                composable(Screen.Transactions.route) {
                    TransactionsScreen(
                        transactions = transactions,
                        onSelectTransaction = { txn ->
                            selectedTransactionForDetail = txn
                        },
                        onShowMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }

                composable(Screen.Budgets.route) {
                    BudgetsScreen(
                        budgetItems = budgets,
                        onUpdateBudgetLimit = onUpdateBudgetLimit,
                        onShowMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        preferences = preferences,
                        transactions = transactions,
                        onUpdatePreferences = onUpdatePreferences,
                        onResetDatabase = onResetDatabase,
                        onClearTransactions = onClearTransactions,
                        onShowMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }
            }
        }

        // Quick Add Bottom Sheet (Keypad + Category + Account)
        if (showQuickAddSheet) {
            QuickAddTransactionSheet(
                sheetState = quickAddSheetState,
                initialType = quickAddInitialType,
                onDismiss = { showQuickAddSheet = false },
                onSaveTransaction = { newTxn ->
                    onAddTransaction(newTxn)
                    showQuickAddSheet = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Saved ${newTxn.category} • ${newTxn.formattedAmount}"
                        )
                    }
                }
            )
        }

        // Transaction Detail Bottom Sheet
        selectedTransactionForDetail?.let { txn ->
            TransactionDetailSheet(
                transaction = txn,
                sheetState = detailSheetState,
                onDismiss = { selectedTransactionForDetail = null },
                onDelete = { deletedId ->
                    onDeleteTransaction(deletedId)
                    selectedTransactionForDetail = null
                    scope.launch {
                        snackbarHostState.showSnackbar("Transaction deleted from database")
                    }
                }
            )
        }

        // In-App Update Bottom Sheet
        if (showUpdateSheet && pendingUpdateInfo != null) {
            UpdateBottomSheet(
                updateInfo = pendingUpdateInfo!!,
                onDismiss = { showUpdateSheet = false },
                onShowMessage = { msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            )
        }
    }
}
