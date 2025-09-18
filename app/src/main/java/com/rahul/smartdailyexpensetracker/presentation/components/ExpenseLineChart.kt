package com.rahul.smartdailyexpensetracker.presentation.components

import android.graphics.drawable.GradientDrawable
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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.rahul.smartdailyexpensetracker.domain.models.ChartData
import android.graphics.Color as AndroidColor

@Composable
fun ExpenseLineChart(
    data: ChartData,
    title: String,
    modifier: Modifier = Modifier,
    onChartReady: ((LineChart) -> Unit)? = null
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
                    LineChart(context).apply {
                        setupLineChart(this)
                        onChartReady?.invoke(this)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                update = { lineChart ->
                    updateLineChart(lineChart, data)
                }
            )
        }
    }
}
private fun setupLineChart(lineChart: LineChart) {
    lineChart.apply {
        description.isEnabled = false
        setTouchEnabled(true)
        setDragEnabled(true)
        setScaleEnabled(true)
        setPinchZoom(true)
        setDrawGridBackground(false)

        // X-axis configuration
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(true)
            gridColor = AndroidColor.LTGRAY
            textColor = AndroidColor.BLACK
            textSize = 10f
        }

        // Y-axis configuration
        axisLeft.apply {
            setDrawGridLines(true)
            gridColor = AndroidColor.LTGRAY
            textColor = AndroidColor.BLACK
            textSize = 10f
        }

        axisRight.isEnabled = false
        legend.isEnabled = true

        // Animation
        animateX(1000)
    }
}

private fun updateLineChart(lineChart: LineChart, data: ChartData) {
    val entries = data.values.mapIndexed { index, value ->
        Entry(index.toFloat(), value)
    }

    val dataSet = LineDataSet(entries, "Daily Expenses").apply {
        color = Color(0xFF2196F3).toArgb()
        setCircleColor(Color(0xFF2196F3).toArgb())
        lineWidth = 2f
        circleRadius = 4f
        setDrawCircleHole(false)
        valueTextSize = 10f
        valueTextColor = AndroidColor.BLACK

        setDrawFilled(true)
        fillDrawable = GradientDrawable().apply {
            colors = intArrayOf(
                Color(0xFF2196F3).copy(alpha = 0.3f).toArgb(),
                AndroidColor.TRANSPARENT
            )
            orientation = GradientDrawable.Orientation.TOP_BOTTOM
        }

        mode = LineDataSet.Mode.CUBIC_BEZIER
        cubicIntensity = 0.2f

        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "₹${String.format("%.0f", value)}"
            }
        }
    }

    val lineData = LineData(dataSet)
    lineChart.data = lineData

    // Set X-axis labels
    lineChart.xAxis.valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return if (index >= 0 && index < data.labels.size) {
                data.labels[index]
            } else ""
        }
    }

    lineChart.invalidate()
}


