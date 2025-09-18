package com.rahul.smartdailyexpensetracker.domain.repository

import com.rahul.smartdailyexpensetracker.domain.models.Expense
import com.rahul.smartdailyexpensetracker.domain.models.ExpenseCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ExpenseRepository {
    fun getExpensesForDate(date: LocalDate): Flow<List<Expense>>
    fun getExpensesForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>
    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>>
    fun getAllExpenses(): Flow<List<Expense>>
    suspend fun getTotalForDate(date: LocalDate): Double
    suspend fun addExpense(expense: Expense)
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun deleteExpenseById(expenseId: String)
    suspend fun searchExpenses(query: String): Flow<List<Expense>>
    suspend fun getExpenseById(expenseId: String): Expense?
}