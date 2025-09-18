package com.rahul.smartdailyexpensetracker.domain.models

data class ReportData(
    val dailySummaries: List<DailyExpensesSummary>,
    val categorySummaries: List<CategorySummary>,
    val totalAmount: Double,
    val expenseCount: Int,
    val averageDaily: Double
)