package com.example.pesatrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pesatrack.data.local.entity.FinancialGoalEntity
import com.example.pesatrack.ui.dialogs.AddGoalDialog
import com.example.pesatrack.ui.theme.EmeraldGreen
import com.example.pesatrack.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: MainViewModel) {
    val currencyInfo by viewModel.currentCurrencyInfo.collectAsState()
    val activeGoals by viewModel.activeGoals.collectAsState()
    val achievedGoals by viewModel.achievedGoals.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalToContribute by remember { mutableStateOf<FinancialGoalEntity?>(null) }
    var contributionAmountText by remember { mutableStateOf("") }

    val displayedGoals = when (selectedTab) {
        0 -> activeGoals // All Active
        1 -> activeGoals.filter { it.goalType == "PURCHASE" } // Purchases (Laptop, Phone, Table)
        2 -> activeGoals.filter { it.goalType == "SAVINGS" } // Savings / Emergency
        3 -> achievedGoals // Completed / Achieved
        else -> activeGoals
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial & Purchase Goals", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active (${activeGoals.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Purchases 💻📱") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Savings 🏦") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Achieved ✓ (${achievedGoals.size})") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (displayedGoals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Stars,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedTab == 3) "No achieved goals yet. Keep going!" else "No goals in this section.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (selectedTab != 3) {
                            Button(onClick = { showAddGoalDialog = true }) {
                                Text("+ Set Target (e.g. Laptop, Phone)")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(displayedGoals, key = { it.id }) { goal ->
                        GoalDetailedCard(
                            goal = goal,
                            currencySymbol = currencyInfo.symbol,
                            onToggleAchieved = { isAchieved ->
                                viewModel.toggleGoalAchieved(goal.id, isAchieved)
                            },
                            onAddProgress = {
                                goalToContribute = goal
                                contributionAmountText = ""
                            },
                            onDelete = { viewModel.deleteGoal(goal) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
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

    // Quick Contribute / Add Progress Dialog
    goalToContribute?.let { goal ->
        AlertDialog(
            onDismissRequest = { goalToContribute = null },
            title = { Text("Add Progress to '${goal.title}'") },
            text = {
                Column {
                    Text(
                        text = "Current: ${currencyInfo.symbol} ${goal.savedAmount} / ${currencyInfo.symbol} ${goal.targetAmount}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = contributionAmountText,
                        onValueChange = { contributionAmountText = it },
                        label = { Text("Amount to Add (${currencyInfo.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toAdd = contributionAmountText.toDoubleOrNull() ?: 0.0
                        if (toAdd > 0) {
                            val newTotal = goal.savedAmount + toAdd
                            viewModel.updateGoalProgress(goal, newTotal)
                        }
                        goalToContribute = null
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToContribute = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun GoalDetailedCard(
    goal: FinancialGoalEntity,
    currencySymbol: String,
    onToggleAchieved: (Boolean) -> Unit,
    onAddProgress: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val percent = (progress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Checkbox / Tick box for achieving goal
                Checkbox(
                    checked = goal.isAchieved,
                    onCheckedChange = onToggleAchieved
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text(if (goal.goalType == "PURCHASE") "Purchase" else "Savings") },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                    if (goal.isAchieved) {
                        Text(
                            text = "Achieved ✓",
                            color = EmeraldGreen,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (goal.isAchieved) EmeraldGreen else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%s %,.2f / %,.2f (%d%%)", currencySymbol, goal.savedAmount, goal.targetAmount, percent),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (!goal.isAchieved) {
                    FilledTonalButton(
                        onClick = onAddProgress,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Money")
                    }
                }
            }
        }
    }
}
