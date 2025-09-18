package com.rahul.smartdailyexpensetracker.presentation.uistate

import com.rahul.smartdailyexpensetracker.domain.models.CategorySummary
import com.rahul.smartdailyexpensetracker.domain.models.ChartData
import com.rahul.smartdailyexpensetracker.domain.models.DailyExpensesSummary

data class ExpenseReportUiState(
    val dailySummaries: List<DailyExpensesSummary> = emptyList(),
    val categorySummaries: List<CategorySummary> = emptyList(),
    val totalAmount: Double = 0.0,
    val expenseCount: Int = 0,
    val averageDaily: Double = 0.0,
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val exportData: String? = null,
    val showExportSuccess: Boolean = false,
    val errorMessage: String? = null,
    val showCharts: Boolean = true,
    val chartType: ChartType = ChartType.BAR,
    val dailyChartData: ChartData? = null,
    val categoryChartData: ChartData? = null
)

enum class ChartType {
    BAR, LINE, PIE
}