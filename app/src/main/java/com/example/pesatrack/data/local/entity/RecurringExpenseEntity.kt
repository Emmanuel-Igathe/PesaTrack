package com.example.pesatrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "Monthly Rent"
    val amount: Double,
    val categoryId: Long,
    val dayOfMonth: Int = 1, // Day to auto-deduct (1-28)
    val isActive: Boolean = true,
    val note: String = ""
)
