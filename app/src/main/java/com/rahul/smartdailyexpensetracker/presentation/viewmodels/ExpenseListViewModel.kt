package com.rahul.smartdailyexpensetracker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.smartdailyexpensetracker.data.preferences.UserPreferences
import com.rahul.smartdailyexpensetracker.domain.models.Expense
import com.rahul.smartdailyexpensetracker.domain.usecases.DeleteExpenseUseCase
import com.rahul.smartdailyexpensetracker.domain.usecases.GetExpensesUseCase
import com.rahul.smartdailyexpensetracker.presentation.uistate.ExpenseListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseListUiState())
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    private val _navigateToDetails = MutableSharedFlow<Expense>()
    val navigateToDetails = _navigateToDetails.asSharedFlow()

    init {
        observeUserPreferences()
        loadExpenses()
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferences.groupByCategory.collect { groupByCategory ->
                _uiState.update { it.copy(groupByCategory = groupByCategory) }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadExpenses()
    }

    fun toggleGroupByCategory() {
        viewModelScope.launch {
            val newValue = !_uiState.value.groupByCategory
            _uiState.update { it.copy(groupByCategory = newValue) }
            userPreferences.setGroupByCategory(newValue)
        }
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val params = GetExpensesUseCase.Params(
                filterType = GetExpensesUseCase.FilterType.BY_DATE,
                date = _uiState.value.selectedDate
            )

            getExpensesUseCase(params)
                .onSuccess { expensesFlow ->
                    expensesFlow.collect { expenses ->
                        _uiState.update {
                            it.copy(
                                expenses = expenses,
                                totalAmount = expenses.sumOf { expense -> expense.amount },
                                totalCount = expenses.size,
                                isLoading = false
                            )
                        }
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load expenses: ${exception.message}"
                        )
                    }
                }
        }
    }

    fun searchExpenses(query: String) {
        _uiState.update { it.copy(searchQuery = query, isLoading = true) }

        viewModelScope.launch {
            val filteredExpenses = _uiState.value.expenses.filter { expense ->
                expense.title.contains(query, ignoreCase = true) ||
                        expense.notes.contains(query, ignoreCase = true) ||
                        expense.category.displayName.contains(query, ignoreCase = true)
            }

            _uiState.update {
                it.copy(
                    expenses = filteredExpenses,
                    totalAmount = filteredExpenses.sumOf { expense -> expense.amount },
                    totalCount = filteredExpenses.size,
                    isLoading = false
                )
            }
        }
    }

    fun onExpenseClicked(expense: Expense) {
        viewModelScope.launch {
            _navigateToDetails.emit(expense)
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
        loadExpenses()
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            deleteExpenseUseCase(expense)
                .onSuccess {
                    _uiState.update {
                        val newList = it.expenses.filter { e -> e.id != expense.id }
                        it.copy(
                            expenses = newList,
                            totalAmount = newList.sumOf { e -> e.amount },
                            totalCount = newList.size
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(errorMessage = "Failed to delete: ${e.message}")
                    }
            }
        }
    }
}