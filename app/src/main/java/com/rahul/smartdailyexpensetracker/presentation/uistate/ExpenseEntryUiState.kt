package com.rahul.smartdailyexpensetracker.presentation.uistate

import android.net.Uri
import com.rahul.smartdailyexpensetracker.domain.models.ExpenseCategory

data class ExpenseEntryUiState(
    val title: String = "",
    val amount: String = "",
    val selectedCategory: ExpenseCategory = ExpenseCategory.STAFF,
    val notes: String = "",
    val receiptImageUris: List<String> = emptyList(),
    val todayTotal: Double = 0.0,
    val currency: String = "₹",
    val checkDuplicates: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showSuccess: Boolean = false,
    val showPhotoPickerDialog: Boolean = false,
    val isCapturingPhoto: Boolean = false,
    val tempCameraImageUri: Uri? = null
)