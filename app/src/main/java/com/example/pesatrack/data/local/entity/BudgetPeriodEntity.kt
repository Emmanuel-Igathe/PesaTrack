package com.example.pesatrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_periods")
data class BudgetPeriodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "October 2026"
    val startDate: Long,
    val endDate: Long,
    val totalIncome: Double = 0.0,
    val savingsTarget: Double = 0.0,
    val isActive: Boolean = true
)
