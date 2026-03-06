package com.dhruvathaide.gridly.ui.components

import android.graphics.Color as AndroidColor
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

data class DriverPositionData(
    val driverAcronym: String,
    val driverNumber: Int,
    val teamColorHex: String,
    val positions: Map<Int, Int> // lapNumber -> position
)

@Composable
fun PositionChart(
    driversData: List<DriverPositionData>,
    modifier: Modifier = Modifier
) {
    if (driversData.isEmpty()) return

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
                    isInverted = true // P1 at top
                    granularity = 1f
                    axisMinimum = 0.5f
                    axisMaximum = 20.5f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "P${value.toInt()}"
                        }
                    }
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val dataSets = driversData.map { driverData ->
                val entries = driverData.positions.entries
                    .sortedBy { it.key }
                    .map { (lap, pos) -> Entry(lap.toFloat(), pos.toFloat()) }

                val color = try {
                    Color(AndroidColor.parseColor("#${driverData.teamColorHex}")).toArgb()
                } catch (e: Exception) {
                    AndroidColor.WHITE
                }

                LineDataSet(entries, driverData.driverAcronym).apply {
                    this.color = color
                    lineWidth = 2.5f
                    setDrawCircles(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.LINEAR
                }
            }

            chart.data = LineData(dataSets.toList())
            chart.invalidate()
        },
        modifier = modifier
    )
}
