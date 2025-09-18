package com.rahul.smartdailyexpensetracker.presentation.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.smartdailyexpensetracker.data.preferences.UserPreferences
import com.rahul.smartdailyexpensetracker.domain.models.Expense
import com.rahul.smartdailyexpensetracker.domain.models.ExpenseCategory
import com.rahul.smartdailyexpensetracker.domain.usecases.AddExpenseUseCase
import com.rahul.smartdailyexpensetracker.domain.usecases.GetDailyTotalUseCase
import com.rahul.smartdailyexpensetracker.presentation.uistate.ExpenseEntryUiState
import com.rahul.smartdailyexpensetracker.presentation.utility.DuplicateExpenseException
import com.rahul.smartdailyexpensetracker.presentation.utility.PhotoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ExpenseEntryViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getDailyTotalUseCase: GetDailyTotalUseCase,
    private val userPreferences: UserPreferences,
    private val photoManager: PhotoManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseEntryUiState())
    val uiState: StateFlow<ExpenseEntryUiState> = _uiState.asStateFlow()

    private val userPreferencesFlow = userPreferences.userPreferencesFlow

    init {
        loadInitialData()
        observeUserPreferences()
    }

    private fun loadInitialData() {
        Log.d("ExpenseEntryViewModel", "Loading initial data")
        viewModelScope.launch {
            try {
                val todayTotal = getDailyTotalUseCase(LocalDate.now()).getOrThrow()
                Log.d("ExpenseEntryViewModel", "Today's total loaded: $todayTotal")
                _uiState.update { it.copy(todayTotal = todayTotal) }
            } catch (e: Exception) {
                Log.e("ExpenseEntryViewModel", "Failed to load today's total", e)
                _uiState.update { it.copy(errorMessage = "Failed to load today's total: ${e.message}") }
            }
        }
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferencesFlow.collect { preferences ->
                Log.d("ExpenseEntryViewModel", "Preferences updated: $preferences")
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedCategory = preferences.lastUsedCategory,
                        currency = preferences.defaultCurrency,
                        checkDuplicates = preferences.enableDuplicateCheck
                    )
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun updateCategory(category: ExpenseCategory) {
        _uiState.update { it.copy(selectedCategory = category) }

        // Save as last used category
        viewModelScope.launch {
            try {
                userPreferences.setLastUsedCategory(category)
            } catch (e: Exception) {
                Log.e("ExpenseEntryViewModel", "Failed to save last used category", e)
            }
        }
    }

    fun updateNotes(notes: String) {
        if (notes.length <= 100) {
            _uiState.update { it.copy(notes = notes) }
        }
    }

    fun addExpense() {
        val currentState = _uiState.value

        // Clear previous errors
        _uiState.update { it.copy(errorMessage = null) }

        // Validate input
        if (currentState.title.isBlank()) {
            val error = "Expense title is required"
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        val amount = currentState.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            val error = "Please enter a valid amount greater than 0"
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val expense = Expense(
                    id = UUID.randomUUID().toString(),
                    title = currentState.title.trim(),
                    amount = amount,
                    category = currentState.selectedCategory,
                    notes = currentState.notes.trim(),
                    receiptImageUris = currentState.receiptImageUris,
                    dateTime = LocalDateTime.now(),
                    date = LocalDate.now()
                )

                val params = AddExpenseUseCase.Params(
                    expense = expense,
                    checkDuplicates = currentState.checkDuplicates
                )

                val result = addExpenseUseCase(params)

                result.fold(
                    onSuccess = {
                        val newTotalResult = getDailyTotalUseCase(LocalDate.now())
                        val newTotal = newTotalResult.getOrElse { currentState.todayTotal + amount }

                        _uiState.update {
                            ExpenseEntryUiState(
                                todayTotal = newTotal,
                                showSuccess = true,
                                selectedCategory = currentState.selectedCategory,
                                currency = currentState.currency,
                                checkDuplicates = currentState.checkDuplicates,
                                isLoading = false
                            )
                        }

                        kotlinx.coroutines.delay(3000)
                        _uiState.update { it.copy(showSuccess = false) }
                    },
                    onFailure = { exception ->
                        val errorMessage = when (exception) {
                            is DuplicateExpenseException -> exception.message ?: "Duplicate expense found"
                            is IllegalArgumentException -> exception.message ?: "Invalid expense data"
                            else -> "Failed to add expense: ${exception.message}"
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = errorMessage
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun toggleDuplicateCheck() {
        viewModelScope.launch {
            val newValue = !_uiState.value.checkDuplicates
            _uiState.update { it.copy(checkDuplicates = newValue) }

            try {
                userPreferences.setDuplicateCheck(newValue)
            } catch (e: Exception) {
                Log.e("ExpenseEntryViewModel", "Failed to save duplicate check preference", e)
            }
        }
    }
    // Photo-related methods
    fun showPhotoPickerDialog() {
        _uiState.update { it.copy(showPhotoPickerDialog = true) }
    }

    fun hidePhotoPickerDialog() {
        _uiState.update { it.copy(showPhotoPickerDialog = false) }
    }

    fun prepareCameraCapture(): Uri? {
        return try {
            val imageFile = photoManager.createImageFile()
            val imageUri = photoManager.getImageUri(imageFile)
            _uiState.update {
                it.copy(
                    tempCameraImageUri = imageUri,
                    isCapturingPhoto = true,
                    showPhotoPickerDialog = false
                )
            }
            imageUri
        } catch (e: Exception) {
            Log.e("ExpenseEntryViewModel", "Failed to prepare camera capture", e)
            _uiState.update {
                it.copy(errorMessage = "Failed to prepare camera: ${e.message}")
            }
            null
        }
    }

    fun onCameraResult(success: Boolean) {
        _uiState.update { it.copy(isCapturingPhoto = false) }

        if (success) {
            val tempUri = _uiState.value.tempCameraImageUri
            if (tempUri != null) {
                addReceiptImage(tempUri.toString())
            }
        }

        _uiState.update { it.copy(tempCameraImageUri = null) }
    }

    fun onGalleryResult(uri: Uri?) {
        _uiState.update { it.copy(showPhotoPickerDialog = false) }

        uri?.let { selectedUri ->
            // Copy image to app storage for persistence
            photoManager.copyImageToAppStorage(selectedUri)?.let { copiedUri ->
                addReceiptImage(copiedUri.toString())
            } ?: run {
                _uiState.update {
                    it.copy(errorMessage = "Failed to save selected image")
                }
            }
        }
    }

    private fun addReceiptImage(imageUri: String) {
        val currentImages = _uiState.value.receiptImageUris
        if (currentImages.size >= 5) { // Limit to 5 images
            _uiState.update {
                it.copy(errorMessage = "Maximum 5 receipt images allowed")
            }
            return
        }

        val updatedImages = currentImages + imageUri
        _uiState.update {
            it.copy(receiptImageUris = updatedImages)
        }
    }

    fun removeReceiptImage(imageUri: String) {
        val updatedImages = _uiState.value.receiptImageUris - imageUri
        _uiState.update {
            it.copy(receiptImageUris = updatedImages)
        }
        try {
            photoManager.deleteImage(Uri.parse(imageUri))
        } catch (e: Exception) {
            Log.e("ExpenseEntryViewModel", "Failed to delete image file", e)
        }
    }
}