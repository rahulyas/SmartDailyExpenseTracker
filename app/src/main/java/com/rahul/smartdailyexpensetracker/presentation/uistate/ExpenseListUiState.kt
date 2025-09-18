package com.rahul.smartdailyexpensetracker.presentation.uistate

import com.rahul.smartdailyexpensetracker.domain.models.Expense
import java.time.LocalDate

data class ExpenseListUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val expenses: List<Expense> = emptyList(),
    val totalAmount: Double = 0.0,
    val totalCount: Int = 0,
    val searchQuery: String = "",
    val groupByCategory: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)