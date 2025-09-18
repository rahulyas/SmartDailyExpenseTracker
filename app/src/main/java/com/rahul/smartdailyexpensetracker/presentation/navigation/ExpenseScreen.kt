package com.rahul.smartdailyexpensetracker.presentation.navigation

sealed class ExpenseScreen(val route: String) {
    data object Entry : ExpenseScreen("expense_entry")
    data object List : ExpenseScreen("expense_list")
    data object Reports : ExpenseScreen("expense_reports")
    data object Settings : ExpenseScreen("settings")
    data object Details : ExpenseScreen("expense_details/{expenseId}") {
        fun createRoute(expenseId: String) = "expense_details/$expenseId"
    }
}
