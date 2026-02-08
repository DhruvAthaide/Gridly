package com.dhruvathaide.gridly.ui.components

import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun SpeedTraceChart(
    data1: List<Float>,
    data2: List<Float>,
    modifier: Modifier = Modifier
) {
    // Neon Colors
    val color1 = androidx.compose.ui.graphics.Color(0xFF00E5FF) // Cyan Neon
    val color2 = androidx.compose.ui.graphics.Color(0xFFFF4081) // Pink Neon

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                // Styling
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setDrawGridBackground(false)
                setBackgroundColor(AndroidColor.TRANSPARENT)

                // Axis Styling
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.textColor = AndroidColor.GRAY
                xAxis.setAvoidFirstLastClipping(true)

                axisLeft.textColor = AndroidColor.GRAY
                axisLeft.setDrawGridLines(false)
                
                axisRight.isEnabled = false
                legend.textColor = AndroidColor.WHITE
            }
        },
        update = { chart ->
            val entries1 = data1.mapIndexed { index, value -> Entry(index.toFloat(), value) }
            val entries2 = data2.mapIndexed { index, value -> Entry(index.toFloat(), value) }

            if (entries1.isNotEmpty() || entries2.isNotEmpty()) {
                val set1 = LineDataSet(entries1, "Driver 1").apply {
                    color = color1.toArgb()
                    lineWidth = 3f
                    setDrawCircles(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    
                    // Gradient Fill
                    setDrawFilled(true)
                    val gradient = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        intArrayOf(color1.copy(alpha = 0.4f).toArgb(), AndroidColor.TRANSPARENT)
                    )
                    fillDrawable = gradient
                }

                val set2 = LineDataSet(entries2, "Driver 2").apply {
                    color = color2.toArgb()
                    lineWidth = 3f
                    setDrawCircles(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    
                    // Gradient Fill
                    setDrawFilled(true)
                    val gradient = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        intArrayOf(color2.copy(alpha = 0.4f).toArgb(), AndroidColor.TRANSPARENT)
                    )
                    fillDrawable = gradient
                }

                val lineData = LineData(set1, set2)
                chart.data = lineData
                chart.invalidate() // Refresh
            }
        }
    )
}
