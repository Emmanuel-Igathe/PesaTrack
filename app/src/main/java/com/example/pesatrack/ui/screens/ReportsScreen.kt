package com.example.pesatrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pesatrack.data.local.entity.CategoryEntity
import com.example.pesatrack.data.local.entity.TransactionEntity
import com.example.pesatrack.ui.components.getCategoryIcon
import com.example.pesatrack.ui.theme.AlertRed
import com.example.pesatrack.ui.theme.EmeraldGreen
import com.example.pesatrack.ui.theme.IncomeBlue
import com.example.pesatrack.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currencyInfo by viewModel.currentCurrencyInfo.collectAsState()
    val period by viewModel.activePeriod.collectAsState()
    val transactions by viewModel.activePeriodTransactions.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var selectedPeriodFilter by remember { mutableStateOf("CURRENT") } // CURRENT, 2_MONTHS, 3_MONTHS, ALL

    val filteredTransactions = remember(transactions, selectedPeriodFilter) {
        val now = System.currentTimeMillis()
        val oneMonthMillis = 30L * 24 * 60 * 60 * 1000
        when (selectedPeriodFilter) {
            "2_MONTHS" -> transactions.filter { it.date >= (now - (2 * oneMonthMillis)) }
            "3_MONTHS" -> transactions.filter { it.date >= (now - (3 * oneMonthMillis)) }
            "ALL" -> transactions
            else -> transactions // Current
        }
    }

    val totalIncome = filteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val netSavings = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) ((netSavings / totalIncome) * 100).coerceAtLeast(0.0).toInt() else 0

    val categoryMap = remember(categories) { categories.associateBy { it.id } }
    val expensesByCategory = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "EXPENSE" }
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Reports & Analytics", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.shareReport(context) }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Report")
                    }
                }
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
            // Period selector (Past 1, 2, 3 months, All)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPeriodFilter == "CURRENT",
                        onClick = { selectedPeriodFilter = "CURRENT" },
                        label = { Text("Current") }
                    )
                    FilterChip(
                        selected = selectedPeriodFilter == "2_MONTHS",
                        onClick = { selectedPeriodFilter = "2_MONTHS" },
                        label = { Text("Past 2 Mo") }
                    )
                    FilterChip(
                        selected = selectedPeriodFilter == "3_MONTHS",
                        onClick = { selectedPeriodFilter = "3_MONTHS" },
                        label = { Text("Past 3 Mo") }
                    )
                    FilterChip(
                        selected = selectedPeriodFilter == "ALL",
                        onClick = { selectedPeriodFilter = "ALL" },
                        label = { Text("All Time") }
                    )
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Period Financial Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Income", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = String.format("%s %,.2f", currencyInfo.symbol, totalIncome),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeBlue
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Expenses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = String.format("%s %,.2f", currencyInfo.symbol, totalExpense),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Net Savings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = String.format("%s %,.2f", currencyInfo.symbol, netSavings),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (netSavings >= 0) EmeraldGreen else AlertRed
                                )
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text("Savings Rate: $savingsRate%") }
                            )
                        }
                    }
                }
            }

            // Download & Share Monthly Report Action
            item {
                Button(
                    onClick = { viewModel.shareReport(context) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download & Share Monthly Statement")
                }
            }

            // Category Spending Breakdown (Bar Chart representation)
            item {
                Text(
                    text = "Spending by Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (expensesByCategory.isEmpty()) {
                item {
                    Text(
                        text = "No expenses recorded in the selected period.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(expensesByCategory) { (catId, amount) ->
                    val cat = categoryMap[catId]
                    val catName = cat?.name ?: "Other"
                    val ratio = if (totalExpense > 0) (amount / totalExpense).toFloat().coerceIn(0f, 1f) else 0f
                    val percent = (ratio * 100).toInt()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AlertRed.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(catName),
                                        contentDescription = null,
                                        tint = AlertRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = catName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$percent% of total expenses",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = String.format("%s %,.2f", currencyInfo.symbol, amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = AlertRed,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
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
}
