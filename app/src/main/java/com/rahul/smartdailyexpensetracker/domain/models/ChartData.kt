package com.rahul.smartdailyexpensetracker.domain.models

import androidx.compose.ui.graphics.Color

data class ChartData(
    val labels: List<String>,
    val values: List<Float>,
    val colors: List<Color> = emptyList()
)