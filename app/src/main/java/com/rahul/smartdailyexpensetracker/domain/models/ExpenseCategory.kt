package com.rahul.smartdailyexpensetracker.domain.models

import androidx.compose.ui.graphics.Color

enum class ExpenseCategory(val displayName: String, val color: Color) {
    STAFF("Staff", Color(0xFF2196F3)),
    TRAVEL("Travel", Color(0xFF4CAF50)),
    FOOD("Food", Color(0xFFFF9800)),
    UTILITY("Utility", Color(0xFF9C27B0))
}