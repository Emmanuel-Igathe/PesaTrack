package com.example.pesatrack.data.repository

import com.example.pesatrack.data.local.dao.*
import com.example.pesatrack.data.local.entity.*
import com.example.pesatrack.data.preferences.UserPreferencesManager
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetPeriodDao: BudgetPeriodDao,
    private val financialGoalDao: FinancialGoalDao,
    private val recurringExpenseDao: RecurringExpenseDao,
    val userPreferences: UserPreferencesManager
) {
    // Transactions
    fun getTransactionsByPeriod(periodId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByPeriod(periodId)

    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByDateRange(start, end)

    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(limit)

    fun getTotalExpensesForPeriod(periodId: Long): Flow<Double?> =
        transactionDao.getTotalExpensesForPeriod(periodId)

    fun getTotalIncomeForPeriod(periodId: Long): Flow<Double?> =
        transactionDao.getTotalIncomeForPeriod(periodId)

    fun getTotalSavingsForPeriod(periodId: Long): Flow<Double?> =
        transactionDao.getTotalSavingsForPeriod(periodId)

    fun getSpentInCategory(categoryId: Long, periodId: Long): Flow<Double?> =
        transactionDao.getSpentInCategory(categoryId, periodId)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insert(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.delete(transaction)

    // Categories
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>> = categoryDao.getCategoriesByType(type)
    suspend fun getCategoryById(id: Long): CategoryEntity? = categoryDao.getById(id)
    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insert(category)
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.update(category)
    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.delete(category)

    // Budget Periods
    fun getAllPeriods(): Flow<List<BudgetPeriodEntity>> = budgetPeriodDao.getAllPeriods()
    fun getActivePeriod(): Flow<BudgetPeriodEntity?> = budgetPeriodDao.getActivePeriod()
    suspend fun insertPeriod(period: BudgetPeriodEntity): Long {
        if (period.isActive) {
            budgetPeriodDao.deactivateAll()
        }
        return budgetPeriodDao.insert(period)
    }
    suspend fun updatePeriod(period: BudgetPeriodEntity) = budgetPeriodDao.update(period)
    suspend fun getPeriodById(id: Long): BudgetPeriodEntity? = budgetPeriodDao.getById(id)

    // Financial Goals
    fun getGoalsByPeriod(periodId: Long): Flow<List<FinancialGoalEntity>> =
        financialGoalDao.getGoalsByPeriod(periodId)

    fun getActiveGoals(): Flow<List<FinancialGoalEntity>> = financialGoalDao.getActiveGoals()
    fun getAchievedGoals(): Flow<List<FinancialGoalEntity>> = financialGoalDao.getAchievedGoals()
    suspend fun insertGoal(goal: FinancialGoalEntity): Long = financialGoalDao.insert(goal)
    suspend fun updateGoal(goal: FinancialGoalEntity) = financialGoalDao.update(goal)
    suspend fun deleteGoal(goal: FinancialGoalEntity) = financialGoalDao.delete(goal)
    suspend fun markGoalAchieved(goalId: Long, isAchieved: Boolean) {
        val goal = financialGoalDao.getById(goalId) ?: return
        financialGoalDao.update(
            goal.copy(
                isAchieved = isAchieved,
                achievedAt = if (isAchieved) System.currentTimeMillis() else null
            )
        )
    }

    // Recurring Expenses (Rent, Electricity, Water, School Transport, etc.)
    fun getAllRecurringExpenses(): Flow<List<RecurringExpenseEntity>> =
        recurringExpenseDao.getAllRecurringExpenses()

    fun getActiveRecurringExpenses(): Flow<List<RecurringExpenseEntity>> =
        recurringExpenseDao.getActiveRecurringExpenses()

    suspend fun insertRecurringExpense(expense: RecurringExpenseEntity): Long =
        recurringExpenseDao.insert(expense)

    suspend fun updateRecurringExpense(expense: RecurringExpenseEntity) =
        recurringExpenseDao.update(expense)

    suspend fun deleteRecurringExpense(expense: RecurringExpenseEntity) =
        recurringExpenseDao.delete(expense)
}
