package com.rahul.smartdailyexpensetracker.presentation.uistate

import com.rahul.smartdailyexpensetracker.domain.models.Expense

sealed class ExpenseDetailsUiState {
    data object Loading : ExpenseDetailsUiState()
    data class Success(val expense: Expense) : ExpenseDetailsUiState()
    data class Error(val message: String) : ExpenseDetailsUiState()
}