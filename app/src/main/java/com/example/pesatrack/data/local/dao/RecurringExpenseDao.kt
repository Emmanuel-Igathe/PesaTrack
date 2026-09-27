package com.example.pesatrack.data.local.dao

import androidx.room.*
import com.example.pesatrack.data.local.entity.RecurringExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: RecurringExpenseEntity): Long

    @Update
    suspend fun update(expense: RecurringExpenseEntity)

    @Delete
    suspend fun delete(expense: RecurringExpenseEntity)

    @Query("SELECT * FROM recurring_expenses ORDER BY name ASC")
    fun getAllRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1 ORDER BY dayOfMonth ASC")
    fun getActiveRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1")
    suspend fun getActiveRecurringExpensesSync(): List<RecurringExpenseEntity>

    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    suspend fun getById(id: Long): RecurringExpenseEntity?
}
