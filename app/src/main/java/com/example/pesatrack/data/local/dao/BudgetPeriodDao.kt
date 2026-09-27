package com.example.pesatrack.data.local.dao

import androidx.room.*
import com.example.pesatrack.data.local.entity.BudgetPeriodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetPeriodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(period: BudgetPeriodEntity): Long

    @Update
    suspend fun update(period: BudgetPeriodEntity)

    @Delete
    suspend fun delete(period: BudgetPeriodEntity)

    @Query("SELECT * FROM budget_periods ORDER BY startDate DESC")
    fun getAllPeriods(): Flow<List<BudgetPeriodEntity>>

    @Query("SELECT * FROM budget_periods WHERE isActive = 1 ORDER BY startDate DESC LIMIT 1")
    fun getActivePeriod(): Flow<BudgetPeriodEntity?>

    @Query("SELECT * FROM budget_periods WHERE id = :id")
    suspend fun getById(id: Long): BudgetPeriodEntity?

    @Query("UPDATE budget_periods SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateAll()

    @Query("SELECT * FROM budget_periods WHERE startDate >= :startDate AND endDate <= :endDate")
    fun getPeriodsInRange(startDate: Long, endDate: Long): Flow<List<BudgetPeriodEntity>>
}
