package com.example.pesatrack.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pesatrack.data.local.entity.*
import com.example.pesatrack.data.preferences.Currencies
import com.example.pesatrack.data.preferences.CurrencyInfo
import com.example.pesatrack.data.repository.FinanceRepository
import com.example.pesatrack.util.BudgetNotificationHelper
import com.example.pesatrack.util.ReportExporter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class BudgetAlertState(
    val hasAlert: Boolean = false,
    val isExceeded: Boolean = false,
    val spentPercentage: Int = 0,
    val message: String = ""
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repository: FinanceRepository,
    private val appContext: Context
) : ViewModel() {

    class Factory(
        private val repository: FinanceRepository,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository, appContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    // User preferences
    val currencyCode: StateFlow<String> = repository.userPreferences.currencyCodeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "KES")

    val userName: StateFlow<String> = repository.userPreferences.userNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Student")

    val budgetAlertsEnabled: StateFlow<Boolean> = repository.userPreferences.budgetAlertsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val currentCurrencyInfo: StateFlow<CurrencyInfo> = currencyCode.map { code ->
        Currencies.getByCode(code)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currencies.list[0])

    // Active Period
    val activePeriod: StateFlow<BudgetPeriodEntity?> = repository.getActivePeriod()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allPeriods: StateFlow<List<BudgetPeriodEntity>> = repository.getAllPeriods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Categories
    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Goals
    val activeGoals: StateFlow<List<FinancialGoalEntity>> = repository.getActiveGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievedGoals: StateFlow<List<FinancialGoalEntity>> = repository.getAchievedGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recurring expenses (rent, electricity, water, transport)
    val recurringExpenses: StateFlow<List<RecurringExpenseEntity>> = repository.getAllRecurringExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions for active period
    val activePeriodTransactions: StateFlow<List<TransactionEntity>> = activePeriod.flatMapLatest { period ->
        if (period != null) {
            repository.getTransactionsByPeriod(period.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary Totals
    val totalExpenses: StateFlow<Double> = activePeriodTransactions.map { txList ->
        txList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncome: StateFlow<Double> = activePeriodTransactions.map { txList ->
        txList.filter { it.type == "INCOME" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalSavings: StateFlow<Double> = activePeriodTransactions.map { txList ->
        txList.filter { it.type == "SAVINGS" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Smart Budget Alert Computation (80% and 100%+ thresholds)
    val budgetAlertState: StateFlow<BudgetAlertState> = combine(
        activePeriod,
        totalExpenses,
        budgetAlertsEnabled
    ) { period, spent, alertsEnabled ->
        if (period == null || period.totalIncome <= 0 || !alertsEnabled) {
            BudgetAlertState()
        } else {
            val budget = period.totalIncome
            val percentage = ((spent / budget) * 100).toInt()
            when {
                spent >= budget -> {
                    BudgetAlertState(
                        hasAlert = true,
                        isExceeded = true,
                        spentPercentage = percentage,
                        message = "⚠️ Budget Exceeded! You have spent ${percentage}% of your budget."
                    )
                }
                percentage >= 80 -> {
                    BudgetAlertState(
                        hasAlert = true,
                        isExceeded = false,
                        spentPercentage = percentage,
                        message = "⚠️ Warning: You have reached ${percentage}% of your period budget!"
                    )
                }
                else -> BudgetAlertState()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetAlertState())

    init {
        // Create an initial active budget period if none exists
        viewModelScope.launch {
            repository.getActivePeriod().firstOrNull().let { existing ->
                if (existing == null) {
                    val cal = Calendar.getInstance()
                    val start = cal.apply { set(Calendar.DAY_OF_MONTH, 1) }.timeInMillis
                    val end = cal.apply {
                        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    }.timeInMillis
                    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

                    repository.insertPeriod(
                        BudgetPeriodEntity(
                            name = monthName,
                            startDate = start,
                            endDate = end,
                            totalIncome = 0.0,
                            savingsTarget = 0.0,
                            isActive = true
                        )
                    )
                }
            }
        }
    }

    fun setCurrency(code: String) {
        viewModelScope.launch {
            repository.userPreferences.setCurrency(code)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            repository.userPreferences.setUserName(name)
        }
    }

    fun setBudgetAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.userPreferences.setBudgetAlertsEnabled(enabled)
        }
    }

    fun setGoalRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.userPreferences.setGoalRemindersEnabled(enabled)
        }
    }

    fun createBudgetPeriod(name: String, income: Double, savingsTarget: Double, start: Long, end: Long) {
        viewModelScope.launch {
            val id = repository.insertPeriod(
                BudgetPeriodEntity(
                    name = name,
                    startDate = start,
                    endDate = end,
                    totalIncome = income,
                    savingsTarget = savingsTarget,
                    isActive = true
                )
            )

            // If user specified an initial income/pocket money, log it as an initial income transaction
            if (income > 0) {
                val incomeCategory = repository.getCategoriesByType("INCOME").first().firstOrNull()
                val catId = incomeCategory?.id ?: 1L
                repository.insertTransaction(
                    TransactionEntity(
                        amount = income,
                        type = "INCOME",
                        categoryId = catId,
                        description = "Pocket Money / Starting Balance",
                        date = System.currentTimeMillis(),
                        periodId = id
                    )
                )
            }
        }
    }

    fun updateActivePeriodBudget(income: Double, savingsTarget: Double) {
        val current = activePeriod.value ?: return
        viewModelScope.launch {
            repository.updatePeriod(
                current.copy(
                    totalIncome = income,
                    savingsTarget = savingsTarget
                )
            )
        }
    }

    fun addTransaction(
        amount: Double,
        type: String,
        categoryId: Long,
        description: String,
        date: Long = System.currentTimeMillis()
    ) {
        val period = activePeriod.value ?: return
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    description = description,
                    date = date,
                    periodId = period.id
                )
            )

            // Check if this new expense triggers the 80% or 100% threshold
            if (type == "EXPENSE" && period.totalIncome > 0 && budgetAlertsEnabled.value) {
                val currentSpent = totalExpenses.value + amount
                val percentage = ((currentSpent / period.totalIncome) * 100).toInt()
                val currSymbol = currentCurrencyInfo.value.symbol

                if (currentSpent >= period.totalIncome) {
                    BudgetNotificationHelper.sendBudgetAlert(
                        appContext,
                        "⚠️ Budget Exceeded!",
                        "You have exceeded your period budget! Total spent: $currSymbol $currentSpent / $currSymbol ${period.totalIncome}"
                    )
                } else if (percentage >= 80) {
                    BudgetNotificationHelper.sendBudgetAlert(
                        appContext,
                        "⚠️ Budget Warning ($percentage%)",
                        "You've spent $percentage% of your budget. $currSymbol ${period.totalIncome - currentSpent} remaining."
                    )
                }
            }
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    fun addGoal(
        title: String,
        targetAmount: Double,
        savedAmount: Double,
        goalType: String,
        deadline: Long?
    ) {
        val periodId = activePeriod.value?.id ?: 1L
        viewModelScope.launch {
            repository.insertGoal(
                FinancialGoalEntity(
                    title = title,
                    targetAmount = targetAmount,
                    savedAmount = savedAmount,
                    goalType = goalType,
                    deadline = deadline,
                    periodId = periodId,
                    isAchieved = savedAmount >= targetAmount && targetAmount > 0
                )
            )
        }
    }

    fun toggleGoalAchieved(goalId: Long, isAchieved: Boolean) {
        viewModelScope.launch {
            repository.markGoalAchieved(goalId, isAchieved)
        }
    }

    fun updateGoalProgress(goal: FinancialGoalEntity, newSavedAmount: Double) {
        viewModelScope.launch {
            val achieved = newSavedAmount >= goal.targetAmount
            repository.updateGoal(
                goal.copy(
                    savedAmount = newSavedAmount,
                    isAchieved = achieved,
                    achievedAt = if (achieved) System.currentTimeMillis() else null
                )
            )
        }
    }

    fun deleteGoal(goal: FinancialGoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun addRecurringExpense(name: String, amount: Double, categoryId: Long, dayOfMonth: Int) {
        viewModelScope.launch {
            repository.insertRecurringExpense(
                RecurringExpenseEntity(
                    name = name,
                    amount = amount,
                    categoryId = categoryId,
                    dayOfMonth = dayOfMonth,
                    isActive = true
                )
            )
        }
    }

    fun deleteRecurringExpense(expense: RecurringExpenseEntity) {
        viewModelScope.launch {
            repository.deleteRecurringExpense(expense)
        }
    }

    // Apply recurring expenses (e.g. rent, electricity, water) to the current period with one tap!
    fun applyRecurringExpensesToCurrentPeriod() {
        val period = activePeriod.value ?: return
        viewModelScope.launch {
            val recurringList = recurringExpenses.value.filter { it.isActive }
            recurringList.forEach { rec ->
                repository.insertTransaction(
                    TransactionEntity(
                        amount = rec.amount,
                        type = "EXPENSE",
                        categoryId = rec.categoryId,
                        description = "[Recurring] ${rec.name}",
                        date = System.currentTimeMillis(),
                        periodId = period.id,
                        isRecurring = true
                    )
                )
            }
        }
    }

    fun shareReport(context: Context) {
        val period = activePeriod.value
        val txs = activePeriodTransactions.value
        val catMap = categories.value.associateBy { it.id }
        val goals = activeGoals.value + achievedGoals.value

        val reportText = ReportExporter.generateReportText(
            currencySymbol = currentCurrencyInfo.value.symbol,
            period = period,
            transactions = txs,
            categories = catMap,
            goals = goals
        )

        ReportExporter.shareReport(
            context = context,
            reportContent = reportText,
            periodName = period?.name ?: "Monthly_Report"
        )
    }
}
