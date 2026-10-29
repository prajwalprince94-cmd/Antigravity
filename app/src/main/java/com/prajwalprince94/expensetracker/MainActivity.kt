package com.prajwalprince94.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prajwalprince94.expensetracker.navigation.Screen
import com.prajwalprince94.expensetracker.ui.budgets.BudgetsScreen
import com.prajwalprince94.expensetracker.ui.components.FloatingBottomNavBar
import com.prajwalprince94.expensetracker.ui.home.HomeScreen
import com.prajwalprince94.expensetracker.ui.settings.SettingsScreen
import com.prajwalprince94.expensetracker.ui.theme.ExpenseTrackerTheme
import com.prajwalprince94.expensetracker.ui.transaction.TransactionsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge system bars (optimized for modern Android smartphones)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                MainApp()
            }
        }
    }
}

/**
 * Main application container for Level 1.
 * - NavHost hosting the 4 core smartphone destinations
 * - Floating Bottom Navigation Bar with single-top navigation and state preservation
 */
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    FloatingBottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { screen ->
                            navController.navigate(screen.route) {
                                // Pop up to start destination to avoid building a deep backstack
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when re-selecting
                                launchSingleTop = true
                                // Restore state when re-selecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen()
                    }
                    composable(Screen.Transactions.route) {
                        TransactionsScreen()
                    }
                    composable(Screen.Budgets.route) {
                        BudgetsScreen()
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen()
                    }
                }
            }
        }
    }
}
