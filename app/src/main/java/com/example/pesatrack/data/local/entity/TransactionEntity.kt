package com.example.pesatrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: String, // "INCOME", "EXPENSE", "SAVINGS"
    val categoryId: Long,
    val description: String,
    val date: Long, // Unix timestamp in millis
    val periodId: Long,
    val isRecurring: Boolean = false
)
