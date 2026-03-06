package com.dhruvathaide.gridly.ui.components

import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

data class DriverLapTimes(
    val driverAcronym: String,
    val driverNumber: Int,
    val teamColorHex: String,
    val lapTimes: Map<Int, Double> // lapNumber -> lapDuration in seconds
)

@Composable
fun LapTimesChart(
    driversData: List<DriverLapTimes>,
    modifier: Modifier = Modifier
) {
    if (driversData.isEmpty()) return

    val colors = listOf(
        Color(0xFF00E5FF), Color(0xFFFF4081), Color(0xFF00E676),
        Color(0xFFFFD740), Color(0xFFAA00FF), Color(0xFFFF6D00),
        Color(0xFF2979FF), Color(0xFFFF1744), Color(0xFF76FF03),
        Color(0xFFFFAB40)
    )

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setBackgroundColor(AndroidColor.TRANSPARENT)
                legend.textColor = AndroidColor.WHITE
                legend.textSize = 10f

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = AndroidColor.GRAY
                    setDrawGridLines(false)
                    granularity = 1f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "L${value.toInt()}"
                        }
                    }
                }

                axisLeft.apply {
                    textColor = AndroidColor.GRAY
                    setDrawGridLines(true)
                    gridColor = AndroidColor.DKGRAY
                    enableGridDashedLine(10f, 10f, 0f)
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val dataSets = driversData.mapIndexed { index, driverData ->
                val entries = driverData.lapTimes.entries
                    .sortedBy { it.key }
                    .map { (lap, time) -> Entry(lap.toFloat(), time.toFloat()) }

                val color = try {
                    Color(AndroidColor.parseColor("#${driverData.teamColorHex}")).toArgb()
                } catch (e: Exception) {
                    colors[index % colors.size].toArgb()
                }

                LineDataSet(entries, driverData.driverAcronym).apply {
                    this.color = color
                    lineWidth = 2f
                    setDrawCircles(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }
            }

            chart.data = LineData(dataSets.toList())
            chart.invalidate()
        },
        modifier = modifier
    )
}
