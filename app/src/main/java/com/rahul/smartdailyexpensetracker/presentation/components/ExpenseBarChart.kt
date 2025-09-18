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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.rahul.smartdailyexpensetracker.domain.models.ChartData
import android.graphics.Color as AndroidColor

@Composable
fun ExpenseBarChart(
    data: ChartData,
    title: String,
    modifier: Modifier = Modifier,
    showValues: Boolean = true,
    onChartReady: ((BarChart) -> Unit)? = null
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
                    BarChart(context).apply {
                        setupBarChart(this)
                        onChartReady?.invoke(this)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                update = { barChart ->
                    updateBarChart(barChart, data, showValues)
                }
            )
        }
    }
}

private fun setupBarChart(barChart: BarChart) {
    barChart.apply {
        description.isEnabled = false
        setTouchEnabled(true)
        setDragEnabled(true)
        setScaleEnabled(true)
        setPinchZoom(false)
        setDrawBarShadow(false)
        setDrawValueAboveBar(true)
        setMaxVisibleValueCount(60)

        // Styling
        setBackgroundColor(AndroidColor.WHITE)

        // X-axis configuration
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            labelCount = 7
            textColor = AndroidColor.BLACK
            textSize = 10f
        }

        // Y-axis configuration
        axisLeft.apply {
            setDrawGridLines(true)
            gridColor = AndroidColor.LTGRAY
            textColor = AndroidColor.BLACK
            textSize = 10f
            axisMinimum = 0f
        }

        axisRight.isEnabled = false
        legend.isEnabled = false

        // Animation
        animateY(1000)
    }
}

private fun updateBarChart(barChart: BarChart, data: ChartData, showValues: Boolean) {
    val entries = data.values.mapIndexed { index, value ->
        BarEntry(index.toFloat(), value)
    }

    val dataSet = BarDataSet(entries, "Expenses").apply {
        colors = data.colors.ifEmpty {
            listOf(Color(0xFF2196F3))
        }.map { it.toArgb() }

        valueTextColor = AndroidColor.BLACK
        valueTextSize = 10f
        setDrawValues(showValues)

        // Bar styling
        setDrawIcons(false)
        barBorderWidth = 1f
        barBorderColor = AndroidColor.GRAY
    }

    val barData = BarData(dataSet).apply {
        barWidth = 0.8f
        setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "₹${String.format("%.0f", value)}"
            }
        })
    }

    barChart.data = barData

    // Set X-axis labels
    barChart.xAxis.valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return if (index >= 0 && index < data.labels.size) {
                data.labels[index]
            } else ""
        }
    }

    barChart.invalidate()
}