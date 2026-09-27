package com.example.pesatrack.data.local.dao

import androidx.room.*
import com.example.pesatrack.data.local.entity.FinancialGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: FinancialGoalEntity): Long

    @Update
    suspend fun update(goal: FinancialGoalEntity)

    @Delete
    suspend fun delete(goal: FinancialGoalEntity)

    @Query("SELECT * FROM financial_goals WHERE periodId = :periodId ORDER BY createdAt DESC")
    fun getGoalsByPeriod(periodId: Long): Flow<List<FinancialGoalEntity>>

    @Query("SELECT * FROM financial_goals WHERE isAchieved = 0 ORDER BY deadline ASC")
    fun getActiveGoals(): Flow<List<FinancialGoalEntity>>

    @Query("SELECT * FROM financial_goals WHERE isAchieved = 1 ORDER BY achievedAt DESC")
    fun getAchievedGoals(): Flow<List<FinancialGoalEntity>>

    @Query("SELECT * FROM financial_goals WHERE id = :id")
    suspend fun getById(id: Long): FinancialGoalEntity?

    @Query("UPDATE financial_goals SET isAchieved = 1, achievedAt = :achievedAt WHERE id = :id")
    suspend fun markAsAchieved(id: Long, achievedAt: Long = System.currentTimeMillis())

    @Query("UPDATE financial_goals SET savedAmount = :amount WHERE id = :id")
    suspend fun updateSavedAmount(id: Long, amount: Double)

    // Goals whose deadline is within next N days and not yet achieved
    @Query("""
        SELECT * FROM financial_goals 
        WHERE isAchieved = 0 
        AND deadline IS NOT NULL 
        AND deadline <= :deadlineThreshold 
        AND deadline >= :now
        ORDER BY deadline ASC
    """)
    suspend fun getUpcomingDeadlineGoals(now: Long, deadlineThreshold: Long): List<FinancialGoalEntity>
}
