package com.prajwalprince94.expensetracker

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prajwalprince94.expensetracker.data.model.AccountItem
import com.prajwalprince94.expensetracker.data.model.CategoryBudgetItem
import com.prajwalprince94.expensetracker.data.model.CategoryItem
import com.prajwalprince94.expensetracker.data.model.SplitBillItem
import com.prajwalprince94.expensetracker.data.model.SplitGroup
import com.prajwalprince94.expensetracker.data.model.SubscriptionItem
import com.prajwalprince94.expensetracker.data.sms.SmsExpenseReceiver
import com.prajwalprince94.expensetracker.data.update.AppUpdateInfo
import com.prajwalprince94.expensetracker.data.update.AppUpdateManager
import com.prajwalprince94.expensetracker.navigation.Screen
import com.prajwalprince94.expensetracker.ui.accounts.AccountsAndCategoriesScreen
import com.prajwalprince94.expensetracker.ui.analytics.AnalyticsScreen
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
import com.prajwalprince94.expensetracker.ui.receipt.ReceiptScannerSheet
import com.prajwalprince94.expensetracker.ui.security.SecurityLockScreen
import com.prajwalprince94.expensetracker.ui.settings.SettingsScreen
import com.prajwalprince94.expensetracker.ui.sms.SmsSyncSheet
import com.prajwalprince94.expensetracker.ui.split.SplitGroupsScreen
import com.prajwalprince94.expensetracker.ui.subscriptions.SubscriptionsScreen
import com.prajwalprince94.expensetracker.ui.theme.ExpenseTrackerTheme
import com.prajwalprince94.expensetracker.ui.transaction.TransactionsScreen
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private var incomingAction by mutableStateOf<String?>(null)

    private fun extractIncomingAction(intent: Intent?): String? {
        if (intent == null) return null
        return intent.action ?: intent.getStringExtra("widgetAction")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingAction = extractIncomingAction(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incomingAction = extractIncomingAction(intent)
        ExpenseGraph.ensureInit(this)
        enableEdgeToEdge()

        setContent {
            val repository = ExpenseGraph.repository
            val preferences by repository.preferences.collectAsState()
            val transactions by repository.transactions.collectAsState()
            val budgets by repository.budgets.collectAsState()
            val accounts by repository.accounts.collectAsState()
            val categories by repository.categories.collectAsState()
            val subscriptions by repository.subscriptions.collectAsState()
            val splitGroups by repository.splitGroups.collectAsState()
            val splitBills by repository.splitBills.collectAsState()
            val isLoaded by repository.isLoaded.collectAsState()

            var hasBooted by remember { mutableStateOf(false) }
            var isAppLocked by remember(preferences.appLockEnabled, preferences.appLockPinHash) {
                mutableStateOf(preferences.appLockEnabled && preferences.appLockPinHash.isNotBlank())
            }
            var lastBackgroundTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }

            // Recents Privacy Shield: secure app window in Android Recents switcher
            LaunchedEffect(preferences.appLockEnabled, preferences.recentsPrivacyBlur) {
                if (preferences.appLockEnabled && preferences.recentsPrivacyBlur) {
                    window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            // Lifecycle monitoring for Auto-Lock timeout on app backgrounding
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner, preferences.appLockEnabled, preferences.appLockPinHash, preferences.autoLockTimeoutSeconds) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_STOP -> {
                            lastBackgroundTimestamp = System.currentTimeMillis()
                        }
                        Lifecycle.Event.ON_START -> {
                            if (preferences.appLockEnabled && preferences.appLockPinHash.isNotBlank()) {
                                val elapsedSec = (System.currentTimeMillis() - lastBackgroundTimestamp) / 1000L
                                if (elapsedSec >= preferences.autoLockTimeoutSeconds) {
                                    isAppLocked = true
                                }
                            }
                        }
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            ExpenseTrackerTheme(preferences = preferences) {
                CompositionLocalProvider(LocalReduceMotion provides preferences.reduceMotion) {
                    Crossfade(
                        targetState = Triple(hasBooted, isAppLocked, preferences.appLockEnabled && preferences.appLockPinHash.isNotBlank()),
                        animationSpec = tween(350),
                        label = "appBootCrossfade"
                    ) { (booted, locked, hasPinLock) ->
                        if (!booted) {
                            BootSequence(
                                isDataLoaded = isLoaded,
                                onBootComplete = { hasBooted = true }
                            )
                        } else if (locked && hasPinLock) {
                            SecurityLockScreen(
                                storedPinHash = preferences.appLockPinHash,
                                isBiometricEnabled = preferences.biometricEnabled,
                                onUnlockSuccess = {
                                    isAppLocked = false
                                    lastBackgroundTimestamp = System.currentTimeMillis()
                                }
                            )
                        } else {
                            MainApp(
                                preferences = preferences,
                                transactions = transactions,
                                budgets = budgets,
                                accounts = accounts,
                                categories = categories,
                                subscriptions = subscriptions,
                                onAddTransaction = { txn ->
                                    ExpenseGraph.appScope.launch {
                                        repository.addTransaction(txn)
                                    }
                                },
                                onAddBatchTransactions = { txns ->
                                    ExpenseGraph.appScope.launch {
                                        repository.addTransactions(txns)
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
                                onAddAccount = { acc ->
                                    ExpenseGraph.appScope.launch {
                                        repository.addAccount(acc)
                                    }
                                },
                                onUpdateAccount = { acc ->
                                    ExpenseGraph.appScope.launch {
                                        repository.updateAccount(acc)
                                    }
                                },
                                onDeleteAccount = { id ->
                                    ExpenseGraph.appScope.launch {
                                        repository.deleteAccount(id)
                                    }
                                },
                                onAddCategory = { cat ->
                                    ExpenseGraph.appScope.launch {
                                        repository.addCategory(cat)
                                    }
                                },
                                onDeleteCategory = { id ->
                                    ExpenseGraph.appScope.launch {
                                        repository.deleteCategory(id)
                                    }
                                },
                                onAddSubscription = { sub ->
                                    ExpenseGraph.appScope.launch {
                                        repository.addSubscription(sub)
                                    }
                                },
                                onUpdateSubscription = { sub ->
                                    ExpenseGraph.appScope.launch {
                                        repository.updateSubscription(sub)
                                    }
                                },
                                onDeleteSubscription = { id ->
                                    ExpenseGraph.appScope.launch {
                                        repository.deleteSubscription(id)
                                    }
                                },
                                onLogSubscriptionPayment = { id ->
                                    ExpenseGraph.appScope.launch {
                                        repository.logSubscriptionPayment(id)
                                    }
                                },
                                splitGroups = splitGroups,
                                splitBills = splitBills,
                                onAddSplitGroup = { grp ->
                                    ExpenseGraph.appScope.launch {
                                        repository.addSplitGroup(grp)
                                    }
                                },
                                onUpdateSplitGroup = { grp ->
                                    ExpenseGraph.appScope.launch {
                                        repository.updateSplitGroup(grp)
                                    }
                                },
                                onDeleteSplitGroup = { id ->
                                    ExpenseGraph.appScope.launch {
                                        repository.deleteSplitGroup(id)
                                    }
                                },
                                onAddSplitBill = { bill ->
                                    ExpenseGraph.appScope.launch {
                                        repository.addSplitBill(bill)
                                    }
                                },
                                onDeleteSplitBill = { id ->
                                    ExpenseGraph.appScope.launch {
                                        repository.deleteSplitBill(id)
                                    }
                                },
                                onSettleDebt = { groupId, from, to, amt, acc ->
                                    ExpenseGraph.appScope.launch {
                                        repository.settleDebt(groupId, from, to, amt, acc)
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
                                },
                                incomingAction = incomingAction,
                                onClearIncomingAction = { incomingAction = null }
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_QUICK_ADD = "com.prajwalprince94.expensetracker.ACTION_QUICK_ADD"
        const val ACTION_SCAN_RECEIPT = "com.prajwalprince94.expensetracker.ACTION_SCAN_RECEIPT"
        const val ACTION_SMS_SYNC = "com.prajwalprince94.expensetracker.ACTION_SMS_SYNC"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    preferences: AppPreferences,
    transactions: List<TransactionItem>,
    budgets: List<CategoryBudgetItem>,
    accounts: List<AccountItem>,
    categories: List<CategoryItem>,
    subscriptions: List<SubscriptionItem>,
    onAddTransaction: (TransactionItem) -> Unit,
    onAddBatchTransactions: (List<TransactionItem>) -> Unit = {},
    onDeleteTransaction: (String) -> Unit,
    onUpdateBudgetLimit: (String, Long) -> Unit,
    onAddAccount: (AccountItem) -> Unit,
    onUpdateAccount: (AccountItem) -> Unit,
    onDeleteAccount: (String) -> Unit,
    onAddCategory: (CategoryItem) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onAddSubscription: (SubscriptionItem) -> Unit,
    onUpdateSubscription: (SubscriptionItem) -> Unit,
    onDeleteSubscription: (String) -> Unit,
    onLogSubscriptionPayment: (String) -> Unit,
    splitGroups: List<SplitGroup> = emptyList(),
    splitBills: List<SplitBillItem> = emptyList(),
    onAddSplitGroup: (SplitGroup) -> Unit = {},
    onUpdateSplitGroup: (SplitGroup) -> Unit = {},
    onDeleteSplitGroup: (String) -> Unit = {},
    onAddSplitBill: (SplitBillItem) -> Unit = {},
    onDeleteSplitBill: (String) -> Unit = {},
    onSettleDebt: (groupId: String, fromMember: String, toMember: String, amountPaise: Long, accountName: String) -> Unit = { _, _, _, _, _ -> },
    onUpdatePreferences: (AppPreferences) -> Unit,
    onResetDatabase: () -> Unit,
    onClearTransactions: () -> Unit,
    incomingAction: String? = null,
    onClearIncomingAction: () -> Unit = {}
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

    var showReceiptScannerSheet by remember { mutableStateOf(false) }
    val receiptScannerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showSmsSyncSheet by remember { mutableStateOf(false) }
    val smsSyncSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(incomingAction) {
        when (incomingAction) {
            MainActivity.ACTION_QUICK_ADD -> {
                showQuickAddSheet = true
                onClearIncomingAction()
            }
            MainActivity.ACTION_SCAN_RECEIPT -> {
                showReceiptScannerSheet = true
                onClearIncomingAction()
            }
            MainActivity.ACTION_SMS_SYNC -> {
                showSmsSyncSheet = true
                onClearIncomingAction()
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            val updateInfo = AppUpdateManager.checkForUpdates(BuildConfig.VERSION_NAME)
            if (updateInfo.isUpdateAvailable) {
                pendingUpdateInfo = updateInfo
                showUpdateSheet = true
            }
        } catch (_: Exception) {}
    }

    LaunchedEffect(Unit) {
        SmsExpenseReceiver.liveDetectedExpenses.collect { liveExpense ->
            snackbarHostState.showSnackbar(
                "SMS Alert: Detected ${liveExpense.formattedAmount} at ${liveExpense.merchantOrBeneficiary}"
            )
        }
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
                        accounts = accounts,
                        subscriptions = subscriptions,
                        splitGroups = splitGroups,
                        splitBills = splitBills,
                        onOpenQuickAdd = { type ->
                            quickAddInitialType = type
                            showQuickAddSheet = true
                        },
                        onOpenReceiptScanner = {
                            showReceiptScannerSheet = true
                        },
                        onOpenSmsSync = {
                            showSmsSyncSheet = true
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
                        onNavigateToAccounts = {
                            navController.navigate(Screen.Accounts.route)
                        },
                        onNavigateToSubscriptions = {
                            navController.navigate(Screen.Subscriptions.route)
                        },
                        onNavigateToAnalytics = {
                            navController.navigate(Screen.Analytics.route)
                        },
                        onNavigateToSplit = {
                            navController.navigate(Screen.SplitGroups.route)
                        },
                        onLogSubscriptionPayment = onLogSubscriptionPayment,
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
                        onOpenReceiptScanner = {
                            showReceiptScannerSheet = true
                        },
                        onOpenSmsSync = {
                            showSmsSyncSheet = true
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
                        onNavigateToAnalytics = {
                            navController.navigate(Screen.Analytics.route)
                        },
                        onShowMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        preferences = preferences,
                        transactions = transactions,
                        accounts = accounts,
                        categories = categories,
                        subscriptions = subscriptions,
                        onNavigateToAccounts = {
                            navController.navigate(Screen.Accounts.route)
                        },
                        onNavigateToSubscriptions = {
                            navController.navigate(Screen.Subscriptions.route)
                        },
                        onNavigateToAnalytics = {
                            navController.navigate(Screen.Analytics.route)
                        },
                        onNavigateToSplit = {
                            navController.navigate(Screen.SplitGroups.route)
                        },
                        onOpenReceiptScanner = {
                            showReceiptScannerSheet = true
                        },
                        onOpenSmsSync = {
                            showSmsSyncSheet = true
                        },
                        onUpdatePreferences = onUpdatePreferences,
                        onResetDatabase = onResetDatabase,
                        onClearTransactions = onClearTransactions,
                        onShowMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }

                composable(Screen.Accounts.route) {
                    AccountsAndCategoriesScreen(
                        accounts = accounts,
                        categories = categories,
                        transactions = transactions,
                        onAddAccount = onAddAccount,
                        onUpdateAccount = onUpdateAccount,
                        onDeleteAccount = onDeleteAccount,
                        onAddCategory = onAddCategory,
                        onDeleteCategory = onDeleteCategory,
                        onNavigateBack = { navController.popBackStack() },
                        onShowMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }

                composable(Screen.Subscriptions.route) {
                    SubscriptionsScreen(
                        subscriptions = subscriptions,
                        accounts = accounts,
                        categories = categories,
                        onAddSubscription = onAddSubscription,
                        onUpdateSubscription = onUpdateSubscription,
                        onDeleteSubscription = onDeleteSubscription,
                        onLogPayment = onLogSubscriptionPayment,
                        onNavigateBack = { navController.popBackStack() },
                        onShowMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }

                composable(Screen.Analytics.route) {
                    AnalyticsScreen(
                        transactions = transactions,
                        budgets = budgets,
                        subscriptions = subscriptions,
                        onNavigateBack = { navController.popBackStack() },
                        onShowMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }

                composable(Screen.SplitGroups.route) {
                    SplitGroupsScreen(
                        groups = splitGroups,
                        bills = splitBills,
                        accounts = accounts,
                        categories = categories,
                        onAddGroup = onAddSplitGroup,
                        onUpdateGroup = onUpdateSplitGroup,
                        onDeleteGroup = onDeleteSplitGroup,
                        onAddBill = onAddSplitBill,
                        onDeleteBill = onDeleteSplitBill,
                        onSettleDebt = onSettleDebt,
                        onNavigateBack = { navController.popBackStack() },
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
                accounts = accounts,
                categories = categories,
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

        // Smart Receipt OCR Scanner Bottom Sheet
        if (showReceiptScannerSheet) {
            ReceiptScannerSheet(
                sheetState = receiptScannerSheetState,
                accounts = accounts,
                categories = categories,
                onDismiss = { showReceiptScannerSheet = false },
                onSaveTransaction = { newTxn ->
                    onAddTransaction(newTxn)
                    showReceiptScannerSheet = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Logged receipt: ${newTxn.title} • ${newTxn.formattedAmount}"
                        )
                    }
                },
                onShowMessage = { msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            )
        }

        // Automated SMS Bank Tracker Bottom Sheet
        if (showSmsSyncSheet) {
            SmsSyncSheet(
                sheetState = smsSyncSheetState,
                existingTransactions = transactions,
                accounts = accounts,
                categories = categories,
                onDismiss = { showSmsSyncSheet = false },
                onSaveTransaction = { newTxn ->
                    onAddTransaction(newTxn)
                    showSmsSyncSheet = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Logged: ${newTxn.title} • ${newTxn.formattedAmount}"
                        )
                    }
                },
                onSaveBatchTransactions = { batch ->
                    onAddBatchTransactions(batch)
                    showSmsSyncSheet = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Auto-logged ${batch.size} transactions from SMS!"
                        )
                    }
                },
                onShowMessage = { msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            )
        }
    }
}
