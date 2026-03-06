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

data class StintLapTime(
    val compound: String,
    val lapNumber: Int,
    val tyreAge: Int, // laps on this set of tyres
    val lapTime: Double
)

data class DriverTyreDeg(
    val driverAcronym: String,
    val stints: List<StintLapTime>
)

private fun compoundColor(compound: String): Int {
    return when (compound.uppercase()) {
        "SOFT" -> Color(0xFFFF1744).toArgb()
        "MEDIUM" -> Color(0xFFFFD740).toArgb()
        "HARD" -> Color.White.toArgb()
        "INTERMEDIATE" -> Color(0xFF4CAF50).toArgb()
        "WET" -> Color(0xFF2196F3).toArgb()
        else -> AndroidColor.GRAY
    }
}

@Composable
fun TyreDegChart(
    driverData: DriverTyreDeg,
    modifier: Modifier = Modifier
) {
    if (driverData.stints.isEmpty()) return

    val stintsByCompound = driverData.stints.groupBy { "${it.compound}_${it.lapNumber - it.tyreAge}" }

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
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
            val dataSets = stintsByCompound.entries.mapIndexed { index, (key, laps) ->
                val compound = laps.first().compound
                val entries = laps.sortedBy { it.tyreAge }
                    .map { Entry(it.lapNumber.toFloat(), it.lapTime.toFloat()) }

                LineDataSet(entries, "${compound.uppercase()} S${index + 1}").apply {
                    color = compoundColor(compound)
                    lineWidth = 2.5f
                    setDrawCircles(true)
                    circleRadius = 3f
                    setCircleColor(color)
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
