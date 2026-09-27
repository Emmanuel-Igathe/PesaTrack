package com.example.pesatrack.data.local.dao

import androidx.room.*
import com.example.pesatrack.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE periodId = :periodId ORDER BY date DESC")
    fun getTransactionsByPeriod(periodId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE periodId = :periodId AND type = :type ORDER BY date DESC")
    fun getTransactionsByPeriodAndType(periodId: Long, type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId AND periodId = :periodId ORDER BY date DESC")
    fun getTransactionsByCategory(categoryId: Long, periodId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE periodId = :periodId AND type = 'EXPENSE'")
    fun getTotalExpensesForPeriod(periodId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE periodId = :periodId AND type = 'INCOME'")
    fun getTotalIncomeForPeriod(periodId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE periodId = :periodId AND type = 'SAVINGS'")
    fun getTotalSavingsForPeriod(periodId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE categoryId = :categoryId AND periodId = :periodId AND type = 'EXPENSE'")
    fun getSpentInCategory(categoryId: Long, periodId: Long): Flow<Double?>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>>

    // For monthly chart data: sum of expenses per day
    @Query("""
        SELECT * FROM transactions 
        WHERE date >= :startDate AND date <= :endDate 
        ORDER BY date ASC
    """)
    fun getTransactionsForReport(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?
}
