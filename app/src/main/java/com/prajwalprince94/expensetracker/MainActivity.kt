package com.prajwalprince94.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prajwalprince94.expensetracker.navigation.Screen
import com.prajwalprince94.expensetracker.ui.budgets.BudgetsScreen
import com.prajwalprince94.expensetracker.ui.components.AuroraBackground
import com.prajwalprince94.expensetracker.ui.components.FloatingBottomNavBar
import com.prajwalprince94.expensetracker.ui.components.LocalReduceMotion
import com.prajwalprince94.expensetracker.ui.components.QuickAddTransactionSheet
import com.prajwalprince94.expensetracker.ui.components.TransactionDetailSheet
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
        enableEdgeToEdge()
        setContent {
            var preferences by remember { mutableStateOf(AppPreferences()) }

            ExpenseTrackerTheme(preferences = preferences) {
                CompositionLocalProvider(LocalReduceMotion provides preferences.reduceMotion) {
                    MainApp(
                        preferences = preferences,
                        onUpdatePreferences = { preferences = it }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    preferences: AppPreferences,
    onUpdatePreferences: (AppPreferences) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Dynamic shared transaction ledger state
    val transactions = remember {
        mutableStateListOf(*TransactionItem.defaultSampleList().toTypedArray())
    }

    // Modal Bottom Sheet States
    var showQuickAddSheet by remember { mutableStateOf(false) }
    var quickAddInitialType by remember { mutableStateOf(TransactionType.EXPENSE) }
    val quickAddSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTransactionForDetail by remember { mutableStateOf<TransactionItem?>(null) }
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Living Aurora Backdrop inspired by NikhilKain/Range
    AuroraBackground(
        modifier = Modifier.fillMaxSize(),
        animated = preferences.ambientAurora && !preferences.reduceMotion
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
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
            // Range fluid horizontal slide + fade transitions
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                enterTransition = {
                    slideInHorizontally(tween(360)) { it / 6 } + fadeIn(tween(260))
                },
                exitTransition = {
                    slideOutHorizontally(tween(360)) { -it / 8 } + fadeOut(tween(200))
                },
                popEnterTransition = {
                    slideInHorizontally(tween(340)) { -it / 8 } + fadeIn(tween(240))
                },
                popExitTransition = {
                    slideOutHorizontally(tween(340)) { it / 6 } + fadeOut(tween(200))
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
                        }
                    )
                }

                composable(Screen.Budgets.route) {
                    BudgetsScreen(
                        onShowMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        preferences = preferences,
                        onUpdatePreferences = onUpdatePreferences,
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
                    transactions.add(0, newTxn)
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
                    transactions.removeAll { it.id == deletedId }
                    selectedTransactionForDetail = null
                    scope.launch {
                        snackbarHostState.showSnackbar("Transaction deleted")
                    }
                }
            )
        }
    }
}
