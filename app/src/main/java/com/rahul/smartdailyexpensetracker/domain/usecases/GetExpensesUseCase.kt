package com.rahul.smartdailyexpensetracker.domain.usecases

import com.rahul.smartdailyexpensetracker.domain.models.Expense
import com.rahul.smartdailyexpensetracker.domain.models.ExpenseCategory
import com.rahul.smartdailyexpensetracker.domain.repository.ExpenseRepository
import com.rahul.smartdailyexpensetracker.presentation.utility.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UseCase<GetExpensesUseCase.Params, Flow<List<Expense>>>(dispatcher) {

    override suspend fun execute(parameters: Params): Flow<List<Expense>> {
        return when (parameters.filterType) {
            FilterType.BY_DATE -> expenseRepository.getExpensesForDate(parameters.date!!)
            FilterType.BY_DATE_RANGE -> expenseRepository.getExpensesForDateRange(
                parameters.startDate!!,
                parameters.endDate!!
            )
            FilterType.BY_CATEGORY -> expenseRepository.getExpensesByCategory(parameters.category!!)
            FilterType.ALL -> expenseRepository.getAllExpenses()
        }
    }

    data class Params(
        val filterType: FilterType,
        val date: LocalDate? = null,
        val startDate: LocalDate? = null,
        val endDate: LocalDate? = null,
        val category: ExpenseCategory? = null
    )

    enum class FilterType {
        BY_DATE, BY_DATE_RANGE, BY_CATEGORY, ALL
    }
}