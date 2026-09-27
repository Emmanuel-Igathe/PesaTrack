package com.example.pesatrack.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.pesatrack.data.local.entity.BudgetPeriodEntity
import com.example.pesatrack.data.local.entity.CategoryEntity
import com.example.pesatrack.data.local.entity.FinancialGoalEntity
import com.example.pesatrack.data.local.entity.TransactionEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    fun generateReportText(
        currencySymbol: String,
        period: BudgetPeriodEntity?,
        transactions: List<TransactionEntity>,
        categories: Map<Long, CategoryEntity>,
        goals: List<FinancialGoalEntity>
    ): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val periodName = period?.name ?: "Financial Report"

        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val netSavings = totalIncome - totalExpense

        val sb = StringBuilder()
        sb.append("=========================================\n")
        sb.append("        PESATRACK FINANCIAL STATEMENT     \n")
        sb.append("=========================================\n")
        sb.append("Period: $periodName\n")
        sb.append("Generated on: ${dateFormat.format(Date())}\n")
        sb.append("-----------------------------------------\n")
        sb.append("SUMMARY:\n")
        sb.append(String.format("• Total Income:   %s %,.2f\n", currencySymbol, totalIncome))
        sb.append(String.format("• Total Expenses: %s %,.2f\n", currencySymbol, totalExpense))
        sb.append(String.format("• Net Balance:    %s %,.2f\n", currencySymbol, netSavings))
        sb.append("-----------------------------------------\n\n")

        // Breakdown by category
        sb.append("CATEGORY BREAKDOWN (EXPENSES):\n")
        val expensesByCategory = transactions.filter { it.type == "EXPENSE" }
            .groupBy { it.categoryId }
        
        if (expensesByCategory.isEmpty()) {
            sb.append("  No expenses recorded for this period.\n")
        } else {
            expensesByCategory.forEach { (catId, txList) ->
                val catName = categories[catId]?.name ?: "Uncategorized"
                val catTotal = txList.sumOf { it.amount }
                val percent = if (totalExpense > 0) (catTotal / totalExpense * 100) else 0.0
                sb.append(String.format("  - %-18s: %s %,.2f (%.1f%%)\n", catName, currencySymbol, catTotal, percent))
            }
        }
        sb.append("\n-----------------------------------------\n")

        // Goals section
        sb.append("FINANCIAL & PURCHASE GOALS:\n")
        if (goals.isEmpty()) {
            sb.append("  No active goals in this period.\n")
        } else {
            goals.forEach { g ->
                val status = if (g.isAchieved) "[ACHIEVED ✓]" else "[IN PROGRESS]"
                val percent = if (g.targetAmount > 0) ((g.savedAmount / g.targetAmount) * 100).toInt() else 0
                sb.append(String.format("  • %-18s: %s %,.2f / %,.2f (%d%%) %s\n",
                    g.title, currencySymbol, g.savedAmount, g.targetAmount, percent, status))
            }
        }
        sb.append("\n-----------------------------------------\n")

        // Itemized Transactions
        sb.append("ITEMIZED TRANSACTIONS:\n")
        if (transactions.isEmpty()) {
            sb.append("  No transactions found.\n")
        } else {
            transactions.forEach { t ->
                val dateStr = dateFormat.format(Date(t.date))
                val catName = categories[t.categoryId]?.name ?: "General"
                val prefix = if (t.type == "INCOME") "+" else "-"
                sb.append(String.format("  [%s] %-12s | %-15s | %s%s %,.2f\n",
                    dateStr, t.type, catName, prefix, currencySymbol, t.amount))
                if (t.description.isNotBlank()) {
                    sb.append("       Note: ${t.description}\n")
                }
            }
        }
        sb.append("\n=========================================\n")
        sb.append("Generated offline via PesaTrack App\n")
        sb.append("=========================================\n")

        return sb.toString()
    }

    fun shareReport(
        context: Context,
        reportContent: String,
        periodName: String
    ) {
        try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val cleanName = periodName.replace(" ", "_").lowercase()
            val file = File(reportsDir, "PesaTrack_Report_${cleanName}.txt")
            file.writeText(reportContent)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "PesaTrack Monthly Report - $periodName")
                putExtra(Intent.EXTRA_TEXT, reportContent)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(sendIntent, "Share Monthly Report Via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to text intent if file provider fails
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "PesaTrack Monthly Report - $periodName")
                putExtra(Intent.EXTRA_TEXT, reportContent)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "Share Monthly Report"))
        }
    }
}
