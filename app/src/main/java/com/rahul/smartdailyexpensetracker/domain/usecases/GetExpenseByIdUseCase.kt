package com.rahul.smartdailyexpensetracker.domain.usecases

import com.rahul.smartdailyexpensetracker.domain.models.Expense
import com.rahul.smartdailyexpensetracker.domain.repository.ExpenseRepository
import com.rahul.smartdailyexpensetracker.presentation.utility.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GetExpenseByIdUseCase @Inject constructor(
    private val repository: ExpenseRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UseCase<String, Expense?>(dispatcher) {

    override suspend fun execute(parameters: String): Expense? {
        return repository.getExpenseById(parameters)
    }
}
