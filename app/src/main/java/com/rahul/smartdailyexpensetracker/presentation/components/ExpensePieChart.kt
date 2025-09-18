package com.rahul.smartdailyexpensetracker.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.rahul.smartdailyexpensetracker.domain.models.ChartData
import com.rahul.smartdailyexpensetracker.domain.models.ExpenseCategory
import android.graphics.Color as AndroidColor

@Composable
fun ExpensePieChart(
    data: ChartData,
    title: String,
    modifier: Modifier = Modifier,
    onChartReady: ((PieChart) -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            AndroidView(
                factory = { context ->
                    PieChart(context).apply {
                        setupPieChart(this)
                        onChartReady?.invoke(this)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                update = { pieChart ->
                    updatePieChart(pieChart, data)
                }
            )
        }
    }
}

private fun setupPieChart(pieChart: PieChart) {
    pieChart.apply {
        setUsePercentValues(true)
        description.isEnabled = false
        setExtraOffsets(5f, 10f, 5f, 5f)
        dragDecelerationFrictionCoef = 0.95f

        setDrawCenterText(true)
        centerText = "Categories"
        setCenterTextSize(16f)
        setCenterTextColor(AndroidColor.BLACK)

        isDrawHoleEnabled = true
        setHoleColor(AndroidColor.WHITE)
        holeRadius = 35f
        setTransparentCircleColor(AndroidColor.WHITE)
        setTransparentCircleAlpha(110)
        transparentCircleRadius = 40f

        setDrawEntryLabels(true)
        setEntryLabelColor(AndroidColor.BLACK)
        setEntryLabelTextSize(10f)

        // Rotation
        rotationAngle = 0f
        isRotationEnabled = true
        isHighlightPerTapEnabled = true

        // Animation
        animateY(1000, Easing.EaseInOutQuad)

        // Legend
        legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            xEntrySpace = 7f
            yEntrySpace = 0f
            yOffset = 0f
            textSize = 10f
            textColor = AndroidColor.BLACK
        }
    }
}

private fun updatePieChart(pieChart: PieChart, data: ChartData) {
    val entries = data.values.mapIndexed { index, value ->
        PieEntry(value, data.labels[index])
    }

    val dataSet = PieDataSet(entries, "Categories").apply {
        setDrawIcons(false)
        sliceSpace = 3f
        iconsOffset = MPPointF(0f, 40f)
        selectionShift = 5f

        // FIXED: Proper color handling for Compose Color to Android Color conversion
        colors = if (data.colors.isNotEmpty()) {
            // Convert Compose Colors to Android Color Ints
            data.colors.map { composeColor ->
                composeColor.toArgb()
            }
        } else {
            // Fallback to default category colors
            ExpenseCategory.values().map { category ->
                category.color.toArgb()
            }
        }

        valueTextColor = AndroidColor.WHITE
        valueTextSize = 12f
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${String.format("%.1f", value)}%"
            }
        }
    }

    val pieData = PieData(dataSet)
    pieChart.data = pieData
    pieChart.invalidate()
}

