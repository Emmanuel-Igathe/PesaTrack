package com.example.pesatrack.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pesatrack.ui.screens.*
import com.example.pesatrack.ui.viewmodel.MainViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Transactions : Screen("transactions", "History", Icons.Default.ReceiptLong)
    object Goals : Screen("goals", "Goals", Icons.Default.Stars)
    object Reports : Screen("reports", "Reports", Icons.Default.BarChart)
    object Customization : Screen("customization", "Budget", Icons.Default.Tune)
}

@Composable
fun RootNavigation(viewModel: MainViewModel) {
    val isRegistered by viewModel.isRegistered.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var showRegisterManual by remember { mutableStateOf(false) }

    Crossfade(targetState = Triple(isRegistered, isLoggedIn, showRegisterManual), label = "AuthNavigation") { (registered, loggedIn, manualRegister) ->
        when {
            // First time user or explicit register switch
            !registered || manualRegister -> {
                RegisterScreen(
                    onRegisterSuccess = { name, phone, email, country, pass ->
                        viewModel.register(name, phone, email, country, pass)
                        showRegisterManual = false
                    },
                    onNavigateToLogin = {
                        if (registered) {
                            showRegisterManual = false
                        }
                    }
                )
            }
            // Registered but locked / logged out
            !loggedIn -> {
                LoginScreen(
                    userName = userProfile.fullName,
                    onLogin = { pass ->
                        viewModel.login(pass)
                    },
                    onNavigateToRegister = {
                        showRegisterManual = true
                    }
                )
            }
            // Fully authenticated
            else -> {
                PesaTrackAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PesaTrackAppScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val screens = listOf(
        Screen.Dashboard,
        Screen.Transactions,
        Screen.Goals,
        Screen.Reports,
        Screen.Customization
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    val selected = currentDestination?.route == screen.route
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToGoals = {
                        navController.navigate(Screen.Goals.route)
                    },
                    onNavigateToTransactions = {
                        navController.navigate(Screen.Transactions.route)
                    }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen(viewModel = viewModel)
            }
            composable(Screen.Goals.route) {
                GoalsScreen(viewModel = viewModel)
            }
            composable(Screen.Reports.route) {
                ReportsScreen(viewModel = viewModel)
            }
            composable(Screen.Customization.route) {
                BudgetCustomizationScreen(viewModel = viewModel)
            }
        }
    }
}
