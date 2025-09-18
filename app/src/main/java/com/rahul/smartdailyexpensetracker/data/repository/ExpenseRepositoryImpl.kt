package com.rahul.smartdailyexpensetracker.data.repository

import android.util.Log
import com.rahul.smartdailyexpensetracker.data.dao.ExpenseDao
import com.rahul.smartdailyexpensetracker.data.preferences.UserPreferences
import com.rahul.smartdailyexpensetracker.domain.models.Expense
import com.rahul.smartdailyexpensetracker.domain.models.ExpenseCategory
import com.rahul.smartdailyexpensetracker.domain.repository.ExpenseRepository
import com.rahul.smartdailyexpensetracker.presentation.utility.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val userPreferences: UserPreferences,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : ExpenseRepository {

    override suspend fun addExpense(expense: Expense) {
        Log.d("ExpenseRepositoryImpl", "Adding expense to database: $expense")

        withContext(dispatcher) {
            try {
                expenseDao.insertExpense(expense)
                Log.d("ExpenseRepositoryImpl", "Expense inserted successfully")

                // Update user preferences with last used category
                userPreferences.setLastUsedCategory(expense.category)
                Log.d("ExpenseRepositoryImpl", "Last used category updated: ${expense.category}")

            } catch (e: Exception) {
                Log.e("ExpenseRepositoryImpl", "Failed to insert expense", e)
                throw e
            }
        }
    }

    override suspend fun getTotalForDate(date: LocalDate): Double {
        return withContext(dispatcher) {
            try {
                val total = expenseDao.getTotalForDate(date) ?: 0.0
                Log.d("ExpenseRepositoryImpl", "Total for $date: $total")
                total
            } catch (e: Exception) {
                Log.e("ExpenseRepositoryImpl", "Failed to get total for date: $date", e)
                0.0
            }
        }
    }

    override fun getExpensesForDate(date: LocalDate): Flow<List<Expense>> {
        Log.d("ExpenseRepositoryImpl", "Getting expenses for date: $date")
        return expenseDao.getExpensesForDate(date)
            .onEach { expenses ->
                Log.d("ExpenseRepositoryImpl", "Found ${expenses.size} expenses for $date")
            }
            .flowOn(dispatcher)
            .catch { e ->
                Log.e("ExpenseRepositoryImpl", "Error getting expenses for date: $date", e)
                emit(emptyList())
            }
    }

    override fun getExpensesForDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Expense>> {
        return expenseDao.getExpensesForDateRange(startDate, endDate)
            .onEach { expenses ->
                Log.d("ExpenseRepositoryImpl", "Found ${expenses.size} expenses in range")
            }
            .flowOn(dispatcher)
            .catch { e ->
                emit(emptyList())
            }
    }

    override fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>> {
        return expenseDao.getExpensesByCategory(category)
            .onEach { expenses ->
                Log.d("ExpenseRepositoryImpl", "Found ${expenses.size} expenses for category $category")
            }
            .flowOn(dispatcher)
            .catch { e ->
                emit(emptyList())
            }
    }

    override fun getAllExpenses(): Flow<List<Expense>> {
        Log.d("ExpenseRepositoryImpl", "Getting all expenses")
        return expenseDao.getAllExpenses()
            .onEach { expenses ->
                Log.d("ExpenseRepositoryImpl", "Found ${expenses.size} total expenses")
            }
            .flowOn(dispatcher)
            .catch { e ->
                emit(emptyList())
            }
    }

    override suspend fun updateExpense(expense: Expense) {
        withContext(dispatcher) {
            try {
                expenseDao.updateExpense(expense)
            } catch (e: Exception) {
                Log.e("ExpenseRepositoryImpl", "Failed to update expense", e)
                throw e
            }
        }
    }

    override suspend fun deleteExpense(expense: Expense) {
        withContext(dispatcher) {
            try {
                expenseDao.deleteExpense(expense)
            } catch (e: Exception) {
                Log.e("ExpenseRepositoryImpl", "Failed to delete expense", e)
                throw e
            }
        }
    }

    override suspend fun deleteExpenseById(expenseId: String) {
        withContext(dispatcher) {
            try {
                expenseDao.deleteExpenseById(expenseId)
            } catch (e: Exception) {
                Log.e("ExpenseRepositoryImpl", "Failed to delete expense by ID: $expenseId", e)
                throw e
            }
        }
    }

    override suspend fun searchExpenses(query: String): Flow<List<Expense>> {
        return expenseDao.searchExpenses("%$query%")
            .onEach { expenses ->
                Log.d("ExpenseRepositoryImpl", "Search found ${expenses.size} expenses")
            }
            .flowOn(dispatcher)
            .catch { e ->
                emit(emptyList())
            }
    }
    override suspend fun getExpenseById(expenseId: String): Expense? {
        return expenseDao.getExpenseById(expenseId)
    }
}