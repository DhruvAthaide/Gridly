package com.dhruvathaide.gridly.ui.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class TelemetryChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint1 = Paint().apply {
        color = Color.CYAN
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
        pathEffect = CornerPathEffect(10f)
    }

    private val paint2 = Paint().apply {
        color = Color.MAGENTA
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
        pathEffect = CornerPathEffect(10f)
    }

    private val fillPaint1 = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val gridPaint = Paint().apply {
        color = Color.parseColor("#33FFFFFF")
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val path1 = Path()
    private val path2 = Path()
    private val fillPath1 = Path()

    // Data points (normalized 0.0 to 1.0)
    private var data1: List<Float> = emptyList()
    private var data2: List<Float> = emptyList()

    fun setData(d1: List<Float>, d2: List<Float>) {
        data1 = d1
        data2 = d2
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val w = width.toFloat()
        val h = height.toFloat()

        // Draw Grid
        canvas.drawLine(0f, h/2, w, h/2, gridPaint)
        canvas.drawLine(0f, h*0.25f, w, h*0.25f, gridPaint)
        canvas.drawLine(0f, h*0.75f, w, h*0.75f, gridPaint)
        
        if (data1.isEmpty() && data2.isEmpty()) return

        // Path 1 (Driver 1)
        drawPath(canvas, data1, path1, paint1, w, h)
        
        // Path 2 (Driver 2)
        drawPath(canvas, data2, path2, paint2, w, h)
        
        // Gradient Fill for Driver 1 (Optional cool effect)
        // Ignoring complicated fill for now to keep performance high
    }

    private fun drawPath(canvas: Canvas, data: List<Float>, path: Path, paint: Paint, w: Float, h: Float) {
        if (data.isEmpty()) return
        
        path.reset()
        val stepX = w / (data.size - 1).coerceAtLeast(1)
        
        path.moveTo(0f, h - (data[0] * h))
        
        for (i in 1 until data.size) {
            val x = i * stepX
            val y = h - (data[i] * h)
            path.lineTo(x, y)
        }
        
        canvas.drawPath(path, paint)
    }
}
