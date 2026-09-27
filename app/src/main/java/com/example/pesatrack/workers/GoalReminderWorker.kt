package com.example.pesatrack.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pesatrack.data.local.database.AppDatabase
import com.example.pesatrack.data.preferences.UserPreferencesManager
import com.example.pesatrack.util.BudgetNotificationHelper
import kotlinx.coroutines.flow.first

class GoalReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = UserPreferencesManager(appContext)
        val remindersEnabled = prefs.goalRemindersEnabledFlow.first()
        if (!remindersEnabled) return Result.success()

        val db = AppDatabase.getInstance(appContext)
        val activeGoals = db.financialGoalDao().getActiveGoals().first()

        if (activeGoals.isNotEmpty()) {
            val randomGoal = activeGoals.random()
            val progressPercent = if (randomGoal.targetAmount > 0) {
                ((randomGoal.savedAmount / randomGoal.targetAmount) * 100).toInt()
            } else 0

            BudgetNotificationHelper.sendGoalReminder(
                context = appContext,
                title = "🎯 Goal Progress: ${randomGoal.title}",
                message = "You have saved ${progressPercent}% towards your goal. Keep going to reach your target!"
            )
        }

        return Result.success()
    }
}
