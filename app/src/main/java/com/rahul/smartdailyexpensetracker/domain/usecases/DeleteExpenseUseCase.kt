package com.rahul.smartdailyexpensetracker.domain.usecases

import com.rahul.smartdailyexpensetracker.data.dao.ExpenseDao
import com.rahul.smartdailyexpensetracker.domain.models.Expense
import com.rahul.smartdailyexpensetracker.presentation.utility.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class DeleteExpenseUseCase @Inject constructor(
    private val expenseDao: ExpenseDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UseCase<Expense, Unit>(dispatcher) {

    override suspend fun execute(parameters: Expense) {
        expenseDao.deleteExpense(parameters)
    }
}
