package com.rahul.smartdailyexpensetracker.domain.repository

import android.graphics.Bitmap
import com.rahul.smartdailyexpensetracker.domain.models.CategorySummary
import com.rahul.smartdailyexpensetracker.domain.models.DailyExpensesSummary

interface ChartGenerator {
    suspend fun generateDailyChart(dailySummaries: List<DailyExpensesSummary>): Bitmap?
    suspend fun generateCategoryChart(categorySummaries: List<CategorySummary>): Bitmap?
}
