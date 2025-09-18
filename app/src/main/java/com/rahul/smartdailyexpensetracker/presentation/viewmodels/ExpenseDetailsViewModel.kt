package com.rahul.smartdailyexpensetracker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.smartdailyexpensetracker.domain.usecases.GetExpenseByIdUseCase
import com.rahul.smartdailyexpensetracker.presentation.uistate.ExpenseDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseDetailsViewModel @Inject constructor(
    private val getExpenseByIdUseCase: GetExpenseByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpenseDetailsUiState>(ExpenseDetailsUiState.Loading)
    val uiState: StateFlow<ExpenseDetailsUiState> = _uiState.asStateFlow()

    fun loadExpense(expenseId: String) {
        viewModelScope.launch {
            _uiState.value = ExpenseDetailsUiState.Loading
            getExpenseByIdUseCase(expenseId)
                .onSuccess { expense ->
                    if (expense != null) {
                        _uiState.value = ExpenseDetailsUiState.Success(expense)
                    } else {
                        _uiState.value = ExpenseDetailsUiState.Error("Expense not found")
                    }
                }
                .onFailure { e ->
                    _uiState.value = ExpenseDetailsUiState.Error(e.message ?: "Unknown error")
                }
        }
    }
}


