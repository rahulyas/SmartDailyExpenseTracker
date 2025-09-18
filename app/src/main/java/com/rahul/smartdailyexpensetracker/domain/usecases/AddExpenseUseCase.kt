package com.rahul.smartdailyexpensetracker.domain.usecases

import android.util.Log
import com.rahul.smartdailyexpensetracker.domain.models.Expense
import com.rahul.smartdailyexpensetracker.domain.repository.ExpenseRepository
import com.rahul.smartdailyexpensetracker.presentation.utility.DuplicateExpenseException
import com.rahul.smartdailyexpensetracker.presentation.utility.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UseCase<AddExpenseUseCase.Params, Unit>(dispatcher) {

    override suspend fun execute(parameters: Params): Unit {
        validateExpense(parameters.expense)
        if (parameters.checkDuplicates) {
            checkForDuplicates(parameters.expense)
        }
        expenseRepository.addExpense(parameters.expense)
    }

    private suspend fun validateExpense(expense: Expense) {
        when {
            expense.title.isBlank() -> {
                throw IllegalArgumentException("Expense title cannot be empty")
            }
            expense.amount <= 0 -> {
                throw IllegalArgumentException("Expense amount must be greater than 0")
            }
            expense.title.length > 100 -> {
                throw IllegalArgumentException("Expense title cannot exceed 100 characters")
            }
        }
    }

    private suspend fun checkForDuplicates(expense: Expense) {
        try {
            val expenses = expenseRepository.getExpensesForDate(expense.date).first()

            val duplicates = expenses.filter { existing ->
                existing.title.equals(expense.title, ignoreCase = true) &&
                        kotlin.math.abs(existing.amount - expense.amount) < 0.01 &&
                        existing.category == expense.category &&
                        existing.dateTime.toLocalDate() == expense.dateTime.toLocalDate()
            }

            if (duplicates.isNotEmpty()) {
                throw DuplicateExpenseException(
                    "A similar expense already exists today: '${expense.title}' for ${expense.amount}"
                )
            }
        } catch (e: DuplicateExpenseException) {
            throw e
        } catch (e: Exception) {
            Log.e("AddExpenseUseCase", "Error checking for duplicates", e)
        }
    }


    data class Params(
        val expense: Expense,
        val checkDuplicates: Boolean = true
    )
}