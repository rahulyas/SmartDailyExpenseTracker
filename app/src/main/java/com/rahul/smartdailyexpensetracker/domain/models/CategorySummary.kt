package com.rahul.smartdailyexpensetracker.domain.models

data class CategorySummary(
    val category: ExpenseCategory,
    val totalAmount: Double,
    val expenseCount: Int
)