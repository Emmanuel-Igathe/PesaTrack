package com.example.pesatrack

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.*
import com.example.pesatrack.data.local.database.AppDatabase
import com.example.pesatrack.data.preferences.UserPreferencesManager
import com.example.pesatrack.data.repository.FinanceRepository
import com.example.pesatrack.workers.GoalReminderWorker
import java.util.concurrent.TimeUnit

class PesaTrackApp : Application() {

    companion object {
        const val CHANNEL_BUDGET_ALERTS = "pesatrack_budget_alerts"
        const val CHANNEL_GOAL_REMINDERS = "pesatrack_goal_reminders"
        lateinit var instance: PesaTrackApp
            private set
    }

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val userPreferences: UserPreferencesManager by lazy { UserPreferencesManager(this) }
    val repository: FinanceRepository by lazy {
        FinanceRepository(
            transactionDao = database.transactionDao(),
            categoryDao = database.categoryDao(),
            budgetPeriodDao = database.budgetPeriodDao(),
            financialGoalDao = database.financialGoalDao(),
            recurringExpenseDao = database.recurringExpenseDao(),
            userPreferences = userPreferences
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
        setupGoalReminderWorker()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET_ALERTS,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when approaching 80% or exceeding your set budget."
            }

            val goalChannel = NotificationChannel(
                CHANNEL_GOAL_REMINDERS,
                "Goal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders about your active purchase and financial goals."
            }

            notificationManager.createNotificationChannels(listOf(budgetChannel, goalChannel))
        }
    }

    private fun setupGoalReminderWorker() {
        val workRequest = PeriodicWorkRequestBuilder<GoalReminderWorker>(
            24, TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "PesatrackGoalReminders",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
