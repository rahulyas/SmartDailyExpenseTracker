package com.rahul.smartdailyexpensetracker.data.preferences

import com.rahul.smartdailyexpensetracker.domain.models.ExpenseCategory
import com.rahul.smartdailyexpensetracker.domain.usecases.ExportExpensesUseCase

data class UserPreferencesData(
    val isDarkTheme: Boolean = false,
    val defaultCurrency: String = "₹",
    val lastUsedCategory: ExpenseCategory = ExpenseCategory.STAFF,
    val enableDuplicateCheck: Boolean = true,
    val enableNotifications: Boolean = true,
    val exportFormat: ExportExpensesUseCase.ExportFormat = ExportExpensesUseCase.ExportFormat.CSV,
    val autoBackup: Boolean = false,
    val dailyReminderTime: String = "18:00",
    val groupByCategory: Boolean = false,
    val showDecimalPlaces: Int = 2
)