package com.rahul.smartdailyexpensetracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rahul.smartdailyexpensetracker.domain.models.Expense
import com.rahul.smartdailyexpensetracker.domain.models.ExpenseCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE date = :date ORDER BY dateTime DESC")
    fun getExpensesForDate(date: LocalDate): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY dateTime DESC")
    fun getExpensesForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY dateTime DESC")
    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date = :date")
    suspend fun getTotalForDate(date: LocalDate): Double?

    @Query("SELECT * FROM expenses WHERE title LIKE :searchQuery OR notes LIKE :searchQuery ORDER BY dateTime DESC")
    fun searchExpenses(searchQuery: String): Flow<List<Expense>>

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: String)

    @Query("SELECT * FROM expenses ORDER BY dateTime DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT COUNT(*) FROM expenses WHERE date = :date")
    suspend fun getExpenseCountForDate(date: LocalDate): Int

    @Query("SELECT AVG(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageExpenseForDateRange(startDate: LocalDate, endDate: LocalDate): Double?

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    suspend fun getExpenseById(expenseId: String): Expense?

}
