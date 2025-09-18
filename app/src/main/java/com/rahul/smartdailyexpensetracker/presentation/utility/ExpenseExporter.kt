package com.rahul.smartdailyexpensetracker.presentation.utility

import com.rahul.smartdailyexpensetracker.domain.models.Expense
import com.rahul.smartdailyexpensetracker.domain.models.ReportData

interface ExpenseExporter {
    suspend fun exportToCsv(
        expenses: List<Expense>,
        onProgress: ((String, Int) -> Unit)? = null
    ): String

    suspend fun exportToJson(
        expenses: List<Expense>,
        onProgress: ((String, Int) -> Unit)? = null
    ): String

    suspend fun generateReportData(expenses: List<Expense>): ReportData
}
