package com.rahul.smartdailyexpensetracker.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.rahul.smartdailyexpensetracker.domain.models.CategorySummary
import com.rahul.smartdailyexpensetracker.domain.models.DailyExpensesSummary
import com.rahul.smartdailyexpensetracker.domain.repository.ChartGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChartGeneratorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ChartGenerator {

    override suspend fun generateDailyChart(dailySummaries: List<DailyExpensesSummary>): Bitmap? {
        return withContext(Dispatchers.Default) {
            try {
                // Create a simple chart bitmap
                // This is a basic implementation - you might want to use a charting library
                val width = 800
                val height = 400
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)

                // Draw white background
                canvas.drawColor(Color.WHITE)

                // Simple chart implementation
                val paint = Paint().apply {
                    color = Color.BLUE
                    strokeWidth = 3f
                    isAntiAlias = true
                }

                // Draw title
                paint.textSize = 24f
                paint.color = Color.BLACK
                canvas.drawText("Daily Expenses", 50f, 50f, paint)

                // Draw simple line chart
                if (dailySummaries.size > 1) {
                    val maxAmount = dailySummaries.maxOf { it.totalAmount }
                    val stepX = (width - 100f) / (dailySummaries.size - 1)
                    val chartHeight = height - 150f

                    paint.color = Color.BLUE
                    paint.style = Paint.Style.STROKE

                    for (i in 0 until dailySummaries.size - 1) {
                        val x1 = 50f + i * stepX
                        val y1 = height - 50f - (dailySummaries[i].totalAmount / maxAmount * chartHeight).toFloat()
                        val x2 = 50f + (i + 1) * stepX
                        val y2 = height - 50f - (dailySummaries[i + 1].totalAmount / maxAmount * chartHeight).toFloat()

                        canvas.drawLine(x1, y1, x2, y2, paint)
                    }
                }

                bitmap
            } catch (e: Exception) {
                Log.e("ChartGenerator", "Failed to generate daily chart", e)
                null
            }
        }
    }

    override suspend fun generateCategoryChart(categorySummaries: List<CategorySummary>): Bitmap? {
        return withContext(Dispatchers.Default) {
            try {
                val width = 800
                val height = 400
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)

                canvas.drawColor(Color.WHITE)

                val paint = Paint().apply {
                    isAntiAlias = true
                }

                // Draw title
                paint.textSize = 24f
                paint.color = Color.BLACK
                canvas.drawText("Category Breakdown", 50f, 50f, paint)

                // Draw simple bar chart
                if (categorySummaries.isNotEmpty()) {
                    val maxAmount = categorySummaries.maxOf { it.totalAmount }
                    val barWidth = (width - 100f) / categorySummaries.size
                    val chartHeight = height - 150f

                    categorySummaries.forEachIndexed { index, category ->
                        val barHeight = (category.totalAmount / maxAmount * chartHeight).toFloat()
                        val left = 50f + index * barWidth
                        val top = height - 50f - barHeight
                        val right = left + barWidth - 10f
                        val bottom = height - 50f

                        paint.color = getColorForCategory(index)
                        paint.style = Paint.Style.FILL
                        canvas.drawRect(left, top, right, bottom, paint)

                        // Draw category label
                        paint.color = Color.BLACK
                        paint.textSize = 12f
                        canvas.drawText(
                            category.category.displayName,
                            left,
                            bottom + 20f,
                            paint
                        )
                    }
                }

                bitmap
            } catch (e: Exception) {
                Log.e("ChartGenerator", "Failed to generate category chart", e)
                null
            }
        }
    }

    private fun getColorForCategory(index: Int): Int {
        val colors = listOf(
            Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA,
            Color.CYAN, Color.YELLOW, Color.GRAY, Color.BLACK
        )
        return colors[index % colors.size]
    }
}