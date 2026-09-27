package com.example.pesatrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_goals")
data class FinancialGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val deadline: Long? = null, // nullable timestamp
    val isAchieved: Boolean = false,
    val goalType: String, // "PURCHASE" or "SAVINGS"
    val periodId: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val achievedAt: Long? = null
)
