package com.rahul.smartdailyexpensetracker.presentation.uistate

import com.rahul.smartdailyexpensetracker.data.preferences.UserPreferencesData

data class SettingsUiState(
    val preferences: UserPreferencesData = UserPreferencesData(),
    val isLoading: Boolean = false,
    val showResetDialog: Boolean = false,
    val resetSuccess: Boolean = false,
    val isExporting: Boolean = false,
    val exportProgress: Int = 0,
    val exportMessage: String = "",
    val exportComplete: Boolean = false,
    val exportError: String? = null
)