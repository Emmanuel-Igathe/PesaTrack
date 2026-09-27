package com.example.pesatrack.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pesatrack.data.local.dao.*
import com.example.pesatrack.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetPeriodEntity::class,
        FinancialGoalEntity::class,
        RecurringExpenseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetPeriodDao(): BudgetPeriodDao
    abstract fun financialGoalDao(): FinancialGoalDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pesatrack_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    seedDefaultCategories(database.categoryDao())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDefaultCategories(dao: CategoryDao) {
            if (dao.count() > 0) return
            val defaults = listOf(
                // Income categories
                CategoryEntity(name = "Salary", icon = "payments", colorHex = "#4CAF50", type = "INCOME"),
                CategoryEntity(name = "Pocket Money", icon = "savings", colorHex = "#2196F3", type = "INCOME"),
                CategoryEntity(name = "Freelance", icon = "work", colorHex = "#00BCD4", type = "INCOME"),
                CategoryEntity(name = "Business", icon = "storefront", colorHex = "#FF9800", type = "INCOME"),
                CategoryEntity(name = "Gift", icon = "card_giftcard", colorHex = "#E91E63", type = "INCOME"),
                CategoryEntity(name = "Other Income", icon = "add_circle", colorHex = "#9C27B0", type = "INCOME"),

                // Expense categories
                CategoryEntity(name = "Rent", icon = "home", colorHex = "#F44336", type = "EXPENSE", monthlyBudget = 0.0),
                CategoryEntity(name = "Food & Groceries", icon = "restaurant", colorHex = "#FF5722", type = "EXPENSE"),
                CategoryEntity(name = "Transport", icon = "directions_bus", colorHex = "#795548", type = "EXPENSE"),
                CategoryEntity(name = "Electricity", icon = "bolt", colorHex = "#FFC107", type = "EXPENSE"),
                CategoryEntity(name = "Water", icon = "water_drop", colorHex = "#03A9F4", type = "EXPENSE"),
                CategoryEntity(name = "Internet", icon = "wifi", colorHex = "#607D8B", type = "EXPENSE"),
                CategoryEntity(name = "Education", icon = "school", colorHex = "#3F51B5", type = "EXPENSE"),
                CategoryEntity(name = "Healthcare", icon = "local_hospital", colorHex = "#E91E63", type = "EXPENSE"),
                CategoryEntity(name = "Entertainment", icon = "movie", colorHex = "#9C27B0", type = "EXPENSE"),
                CategoryEntity(name = "Clothing", icon = "checkroom", colorHex = "#FF4081", type = "EXPENSE"),
                CategoryEntity(name = "Airtime & Data", icon = "smartphone", colorHex = "#00E5FF", type = "EXPENSE"),
                CategoryEntity(name = "Savings", icon = "savings", colorHex = "#66BB6A", type = "EXPENSE"),
                CategoryEntity(name = "Other Expenses", icon = "more_horiz", colorHex = "#9E9E9E", type = "EXPENSE"),
            )
            dao.insertAll(defaults)
        }
    }
}
