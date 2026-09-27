package com.example.pesatrack.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pesatrack.data.local.entity.FinancialGoalEntity
import com.example.pesatrack.data.local.entity.TransactionEntity
import com.example.pesatrack.ui.components.SmartAlertBanner
import com.example.pesatrack.ui.components.StatCard
import com.example.pesatrack.ui.components.getCategoryIcon
import com.example.pesatrack.ui.dialogs.AddGoalDialog
import com.example.pesatrack.ui.dialogs.AddTransactionDialog
import com.example.pesatrack.ui.dialogs.CurrencyPickerDialog
import com.example.pesatrack.ui.theme.*
import com.example.pesatrack.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToGoals: () -> Unit,
    onNavigateToTransactions: () -> Unit
) {
    val currencyInfo by viewModel.currentCurrencyInfo.collectAsState()
    val period by viewModel.activePeriod.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val totalSavings by viewModel.totalSavings.collectAsState()
    val transactions by viewModel.activePeriodTransactions.collectAsState()
    val activeGoals by viewModel.activeGoals.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val alertState by viewModel.budgetAlertState.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var showAddTxDialog by remember { mutableStateOf(false) }
    var initialTxType by remember { mutableStateOf("EXPENSE") }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    val netBalance = totalIncome - totalExpenses
    val budgetLimit = period?.totalIncome ?: 0.0
    val budgetSpentRatio = if (budgetLimit > 0) (totalExpenses / budgetLimit).toFloat().coerceIn(0f, 1f) else 0f

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hello, $userName 👋",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = period?.name ?: "Current Period",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Currency Flag & Picker chip
                    AssistChip(
                        onClick = { showCurrencyDialog = true },
                        label = { Text("${currencyInfo.flag} ${currencyInfo.code}") },
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    initialTxType = "EXPENSE"
                    showAddTxDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Log Expense") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Smart Budget Alert (80% warning or Over-budget)
            item {
                AnimatedVisibility(visible = alertState.hasAlert) {
                    SmartAlertBanner(
                        isExceeded = alertState.isExceeded,
                        message = alertState.message
                    )
                }
            }

            // Overview Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Net Available Balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%s %,.2f", currencyInfo.symbol, netBalance),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Budget usage progress
                        if (budgetLimit > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Period Budget Spent",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "${((totalExpenses / budgetLimit) * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { budgetSpentRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (totalExpenses >= budgetLimit) AlertRed else AlertAmber,
                                trackColor = Color.White.copy(alpha = 0.3f),
                            )
                        }
                    }
                }
            }

            // Stat Cards (Income / Pocket Money & Expenses)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Income",
                        amount = totalIncome,
                        currencySymbol = currencyInfo.symbol,
                        color = IncomeBlue,
                        icon = Icons.Default.ArrowDownward,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Spent",
                        amount = totalExpenses,
                        currencySymbol = currencyInfo.symbol,
                        color = AlertRed,
                        icon = Icons.Default.ArrowUpward,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            initialTxType = "INCOME"
                            showAddTxDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Income")
                    }

                    OutlinedButton(
                        onClick = {
                            initialTxType = "SAVINGS"
                            showAddTxDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Savings")
                    }

                    Button(
                        onClick = { viewModel.applyRecurringExpensesToCurrentPeriod() },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apply Bills")
                    }
                }
            }

            // Financial & Purchase Goals Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎯 Financial & Purchase Goals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToGoals) {
                        Text("See All")
                    }
                }
            }

            if (activeGoals.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No active goals yet (e.g. Laptop, Phone, Table)", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { showAddGoalDialog = true }) {
                                Text("+ Set a Goal")
                            }
                        }
                    }
                }
            } else {
                items(activeGoals.take(3)) { goal ->
                    GoalDashboardItem(
                        goal = goal,
                        currencySymbol = currencyInfo.symbol,
                        onCheckedChange = { isAchieved ->
                            viewModel.toggleGoalAchieved(goal.id, isAchieved)
                        }
                    )
                }
            }

            // Recent Transactions Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToTransactions) {
                        Text("View All")
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        text = "No transactions logged in this period yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(transactions.take(5)) { tx ->
                    val category = categories.firstOrNull { it.id == tx.categoryId }
                    val catName = category?.name ?: "General"
                    val isIncome = tx.type == "INCOME"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isIncome) IncomeBlue.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(catName),
                                    contentDescription = null,
                                    tint = if (isIncome) IncomeBlue else AlertRed
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (tx.description.isNotBlank()) tx.description else catName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = dateFormat.format(Date(tx.date)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = String.format("%s%s %,.2f", if (isIncome) "+" else "-", currencyInfo.symbol, tx.amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isIncome) IncomeBlue else AlertRed
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Dialogs
    if (showAddTxDialog) {
        AddTransactionDialog(
            initialType = initialTxType,
            categories = categories,
            currencySymbol = currencyInfo.symbol,
            onDismiss = { showAddTxDialog = false },
            onConfirm = { amount, type, categoryId, desc ->
                viewModel.addTransaction(amount, type, categoryId, desc)
            }
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            currencySymbol = currencyInfo.symbol,
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, target, saved, type ->
                viewModel.addGoal(title, target, saved, type, null)
            }
        )
    }

    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            currentCode = currencyInfo.code,
            onDismiss = { showCurrencyDialog = false },
            onSelect = { code ->
                viewModel.setCurrency(code)
            }
        )
    }
}

@Composable
fun GoalDashboardItem(
    goal: FinancialGoalEntity,
    currencySymbol: String,
    onCheckedChange: (Boolean) -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val percent = (progress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = goal.isAchieved,
                onCheckedChange = onCheckedChange
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldGreen
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = EmeraldGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("%s %,.0f / %,.0f", currencySymbol, goal.savedAmount, goal.targetAmount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
