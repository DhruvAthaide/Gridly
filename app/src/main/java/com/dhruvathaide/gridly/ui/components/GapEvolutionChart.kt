package com.dhruvathaide.gridly.ui.components

import android.graphics.Color
import android.graphics.DashPathEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

@Composable
fun GapEvolutionChart(
    gapHistory: List<Float>, // Positive values mean Driver 1 is ahead? Or just absolute gap? Let's assume absolute gap.
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(false)
                setDrawGridBackground(false)
                axisRight.isEnabled = false
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    textColor = Color.GRAY
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "L${value.toInt()}" // Lap number relative (can be improved)
                        }
                    }
                }
                
                axisLeft.apply {
                    textColor = Color.WHITE
                    setDrawGridLines(true)
                    gridColor = Color.parseColor("#333333")
                    gridLineWidth = 1f
                    enableGridDashedLine(10f, 10f, 0f)
                }
            }
        },
        update = { chart ->
            if (gapHistory.isEmpty()) return@AndroidView

            val entries = gapHistory.mapIndexed { index, gap ->
                Entry(index.toFloat(), gap)
            }

            val dataSet = LineDataSet(entries, "Gap").apply {
                color = Color.parseColor("#00E5FF") // Cyan (Tron/Cyberpunk)
                lineWidth = 3f
                setDrawCircles(true)
                circleRadius = 4f
                setCircleColor(Color.parseColor("#00E5FF"))
                setDrawCircleHole(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curve
                setDrawValues(false)
                
                // Gradient Fill
                setDrawFilled(true)
                fillColor = Color.parseColor("#00E5FF")
                fillAlpha = 50
            }

            chart.data = LineData(dataSet)
            chart.invalidate() // Refresh
        }
    )
}
