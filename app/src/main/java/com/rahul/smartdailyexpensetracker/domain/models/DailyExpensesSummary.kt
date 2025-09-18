package com.rahul.smartdailyexpensetracker.domain.models

import java.time.LocalDate

data class DailyExpensesSummary(
    val date: LocalDate,
    val totalAmount: Double,
    val expenseCount: Int,
    val expenses: List<Expense>
)