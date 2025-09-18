package com.rahul.smartdailyexpensetracker.presentation.viewmodels

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.rahul.smartdailyexpensetracker.data.preferences.UserPreferences
import com.rahul.smartdailyexpensetracker.domain.models.CategorySummary
import com.rahul.smartdailyexpensetracker.domain.models.ChartData
import com.rahul.smartdailyexpensetracker.domain.models.DailyExpensesSummary
import com.rahul.smartdailyexpensetracker.domain.models.ReportData
import com.rahul.smartdailyexpensetracker.domain.usecases.ExportExpensesUseCase
import com.rahul.smartdailyexpensetracker.domain.usecases.GenerateReportUseCase
import com.rahul.smartdailyexpensetracker.presentation.uistate.ChartType
import com.rahul.smartdailyexpensetracker.presentation.uistate.ExpenseReportUiState
import com.rahul.smartdailyexpensetracker.presentation.utility.SimplePdfExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.lang.ref.WeakReference
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ExpenseReportViewModel @Inject constructor(
    private val generateReportUseCase: GenerateReportUseCase,
    private val userPreferences: UserPreferences,
    private val pdfExportManager: SimplePdfExportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseReportUiState())
    val uiState: StateFlow<ExpenseReportUiState> = _uiState.asStateFlow()

    private var exportFormat = ExportExpensesUseCase.ExportFormat.PDF
    private var dailyChartRef: WeakReference<BarChart>? = null
    private var categoryChartRef: WeakReference<PieChart>? = null

    init {
        observeUserPreferences()
        loadReport()
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferences.userPreferencesFlow.collect { preferences ->
                exportFormat = preferences.exportFormat
            }
        }
    }

    private fun loadReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(6)

            val params = GenerateReportUseCase.Params(
                startDate = startDate,
                endDate = endDate
            )

            generateReportUseCase(params)
                .onSuccess { reportData ->
                    val dailyChartData = prepareDailyChartData(reportData.dailySummaries)
                    val categoryChartData = prepareCategoryChartData(reportData.categorySummaries)

                    _uiState.update {
                        it.copy(
                            dailySummaries = reportData.dailySummaries,
                            categorySummaries = reportData.categorySummaries,
                            totalAmount = reportData.totalAmount,
                            expenseCount = reportData.expenseCount,
                            averageDaily = reportData.averageDaily,
                            dailyChartData = dailyChartData,
                            categoryChartData = categoryChartData,
                            isLoading = false
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to generate report: ${exception.message}"
                        )
                    }
                }
        }
    }

    fun setDailyChartRef(chart: BarChart) {
        dailyChartRef = WeakReference(chart)
    }

    fun setCategoryChartRef(chart: PieChart) {
        categoryChartRef = WeakReference(chart)
    }

    fun toggleChartType() {
        val currentType = _uiState.value.chartType
        val newType = when (currentType) {
            ChartType.BAR -> ChartType.LINE
            ChartType.LINE -> ChartType.PIE
            ChartType.PIE -> ChartType.BAR
        }
        _uiState.update { it.copy(chartType = newType) }
    }

    fun toggleChartsVisibility() {
        _uiState.update { it.copy(showCharts = !it.showCharts) }
    }

    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }

            try {
                val currentState = _uiState.value
                val reportData = ReportData(
                    dailySummaries = currentState.dailySummaries,
                    categorySummaries = currentState.categorySummaries,
                    totalAmount = currentState.totalAmount,
                    expenseCount = currentState.expenseCount,
                    averageDaily = currentState.averageDaily
                )

                // Capture chart bitmaps
                val dailyChartBitmap = captureChartBitmap(dailyChartRef?.get())
                val categoryChartBitmap = captureChartBitmap(categoryChartRef?.get())

                val filePath = pdfExportManager.shareExpenseReportPdf(
                    reportData = reportData,
                    dailyChartBitmap = dailyChartBitmap,
                    categoryChartBitmap = categoryChartBitmap
                )

                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportData = filePath,
                        showExportSuccess = true
                    )
                }

                kotlinx.coroutines.delay(3000)
                _uiState.update { it.copy(showExportSuccess = false) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        errorMessage = "Export failed: ${e.message}"
                    )
                }
            }
        }
    }

    private fun captureChartBitmap(chartView: Any?): Bitmap? {
        return try {
            when (chartView) {
                is BarChart -> {
                    val bitmap = Bitmap.createBitmap(
                        chartView.width,
                        chartView.height,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    chartView.draw(canvas)
                    bitmap
                }
                is PieChart -> {
                    val bitmap = Bitmap.createBitmap(
                        chartView.width,
                        chartView.height,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    chartView.draw(canvas)
                    bitmap
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e("ExpenseReportViewModel", "Failed to capture chart bitmap", e)
            null
        }
    }

    fun shareReport(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Expense Report")
                putExtra(Intent.EXTRA_TEXT, "Please find attached expense report.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Expense Report"))
        } catch (e: Exception) {
            Log.e("ExpenseReportViewModel", "Failed to share report", e)
        }
    }

    fun refreshReport() {
        loadReport()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun prepareDailyChartData(dailySummaries: List<DailyExpensesSummary>): ChartData {
        val labels = dailySummaries.map {
            it.date.format(DateTimeFormatter.ofPattern("MMM dd"))
        }
        val values = dailySummaries.map { it.totalAmount.toFloat() }

        return ChartData(
            labels = labels,
            values = values,
            colors = listOf(Color(0xFF2196F3))
        )
    }

    fun prepareCategoryChartData(categorySummaries: List<CategorySummary>): ChartData {
        val totalAmount = categorySummaries.sumOf { it.totalAmount }
        val labels = categorySummaries.map { it.category.displayName }
        val values = categorySummaries.map {
            ((it.totalAmount / totalAmount) * 100).toFloat()
        }
        val colors = categorySummaries.map { it.category.color }

        return ChartData(
            labels = labels,
            values = values,
            colors = colors
        )
    }
}
