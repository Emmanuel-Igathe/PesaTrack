package com.example.pesatrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String, // Material icon name
    val colorHex: String, // e.g. "#FF6B6B"
    val type: String, // "INCOME" or "EXPENSE"
    val isCustom: Boolean = false,
    val monthlyBudget: Double = 0.0 // 0 = no limit
)
