package com.rahul.smartdailyexpensetracker.domain.usecases

import com.rahul.smartdailyexpensetracker.domain.models.CategorySummary
import com.rahul.smartdailyexpensetracker.domain.models.DailyExpensesSummary
import com.rahul.smartdailyexpensetracker.domain.models.Expense
import com.rahul.smartdailyexpensetracker.domain.models.ReportData
import com.rahul.smartdailyexpensetracker.domain.repository.ExpenseRepository
import com.rahul.smartdailyexpensetracker.presentation.utility.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GenerateReportUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UseCase<GenerateReportUseCase.Params, ReportData>(dispatcher) {

    override suspend fun execute(parameters: Params): ReportData {
        val expenses = expenseRepository.getExpensesForDateRange(
            parameters.startDate,
            parameters.endDate
        )

        return expenses.map { expenseList ->
            val dailySummaries = generateDailySummaries(expenseList)
            val categorySummaries = generateCategorySummaries(expenseList)
            val totalAmount = expenseList.sumOf { it.amount }

            ReportData(
                dailySummaries = dailySummaries,
                categorySummaries = categorySummaries,
                totalAmount = totalAmount,
                expenseCount = expenseList.size,
                averageDaily = totalAmount / ChronoUnit.DAYS.between(parameters.startDate, parameters.endDate).coerceAtLeast(1)
            )
        }.first() // Take first emission for this use case
    }

    private fun generateDailySummaries(expenses: List<Expense>): List<DailyExpensesSummary> {
        return expenses.groupBy { it.date }
            .map { (date, dayExpenses) ->
                DailyExpensesSummary(
                    date = date,
                    totalAmount = dayExpenses.sumOf { it.amount },
                    expenseCount = dayExpenses.size,
                    expenses = dayExpenses
                )
            }
            .sortedBy { it.date }
    }

    private fun generateCategorySummaries(expenses: List<Expense>): List<CategorySummary> {
        return expenses.groupBy { it.category }
            .map { (category, categoryExpenses) ->
                CategorySummary(
                    category = category,
                    totalAmount = categoryExpenses.sumOf { it.amount },
                    expenseCount = categoryExpenses.size
                )
            }
            .sortedByDescending { it.totalAmount }
    }

    data class Params(
        val startDate: LocalDate,
        val endDate: LocalDate
    )
}