package com.rahul.smartdailyexpensetracker.domain.usecases

import com.rahul.smartdailyexpensetracker.domain.repository.ChartGenerator
import com.rahul.smartdailyexpensetracker.domain.repository.ExpenseRepository
import com.rahul.smartdailyexpensetracker.presentation.utility.ExpenseExporter
import com.rahul.smartdailyexpensetracker.presentation.utility.IoDispatcher
import com.rahul.smartdailyexpensetracker.presentation.utility.SimplePdfExportManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class ExportExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val expenseExporter: ExpenseExporter,
    private val simplePdfExportManager: SimplePdfExportManager,
    private val chartGenerator: ChartGenerator? = null, // Optional chart generator
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UseCase<ExportExpensesUseCase.Params, ExportExpensesUseCase.Result>(dispatcher) {

    public override suspend fun execute(parameters: Params): Result {
        val expenses = expenseRepository.getExpensesForDateRange(
            parameters.startDate,
            parameters.endDate
        ).first()

        // Update progress callback
        parameters.onProgress?.invoke("Preparing export...", 10)

        val filePath = when (parameters.format) {
            ExportFormat.CSV -> {
                parameters.onProgress?.invoke("Exporting to CSV...", 30)
                expenseExporter.exportToCsv(expenses, parameters.onProgress)
            }
            ExportFormat.JSON -> {
                parameters.onProgress?.invoke("Exporting to JSON...", 30)
                expenseExporter.exportToJson(expenses, parameters.onProgress)
            }
            ExportFormat.PDF -> {
                parameters.onProgress?.invoke("Generating report data...", 20)
                val reportData = expenseExporter.generateReportData(expenses)

                parameters.onProgress?.invoke("Generating charts...", 40)
                val dailyChart = chartGenerator?.generateDailyChart(reportData.dailySummaries)
                val categoryChart = chartGenerator?.generateCategoryChart(reportData.categorySummaries)

                parameters.onProgress?.invoke("Creating PDF...", 70)
                simplePdfExportManager.generateExpenseReportPdf(
                    reportData = reportData,
                    dailyChartBitmap = dailyChart,
                    categoryChartBitmap = categoryChart
                )
            }
        }

        parameters.onProgress?.invoke("Export complete!", 100)
        return Result(filePath, expenses.size)
    }

    data class Params(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val format: ExportFormat,
        val onProgress: ((String, Int) -> Unit)? = null
    )

    data class Result(
        val filePath: String,
        val recordCount: Int
    )

    enum class ExportFormat {
        CSV, JSON, PDF
    }
}